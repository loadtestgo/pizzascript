/*
 * PizzaScript SSIM for browser filmstrips.  Adapted from a version
 * include with VP8 that takes color components into account.
 *
 *  Copyright (c) 2010 The VP8 project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#include <math.h>
#include <stdio.h>
#include <string.h>

#include "ssim.h"
#include "mlib.h"

// Constants in the SSIM index formula defualt value: K = [0.01 0.03]
// C1 = (K(1)*L)^2  where L is the dynamic range
// C2 = (K(2)*L)^2
#define C1 (float)(64 * 64 * 0.01*255*0.01*255)
#define C2 (float)(64 * 64 * 0.03*255*0.03*255)

SSIM_CONTEXT* ssim_init(int maxWidth, int maxHeight) {
    SSIM_CONTEXT* context = ML_ALLOC_S(SSIM_CONTEXT);
    context->width_uv = 0;
    context->width_y = 0;
    context->height_y = 0;

    int k = 8;
    int rows = k + 1;

    context->img1_sum = ML_ALLOC(short, rows*maxWidth);
    context->img2_sum = ML_ALLOC(short, rows*maxWidth);
    context->img1_sq_sum = ML_ALLOC(int, rows*maxWidth);
    context->img2_sq_sum = ML_ALLOC(int, rows*maxWidth);
    context->img12_mul_sum = ML_ALLOC(int, rows*maxWidth);

    return context;
}

void ssim_destroy(SSIM_CONTEXT* context) {
    ML_FREE(context->img1_sum);
    ML_FREE(context->img2_sum);
    ML_FREE(context->img1_sq_sum);
    ML_FREE(context->img2_sq_sum);
    ML_FREE(context->img12_mul_sum);
    ML_FREE(context);
}

static double vp8_similarity(int mu_x, int mu_y, int pre_mu_x2, int pre_mu_y2,int pre_mu_xy2)
{
    int mu_x2, mu_y2, mu_xy, theta_x2, theta_y2, theta_xy;

    mu_x2 = mu_x * mu_x;
    mu_y2 = mu_y * mu_y;
    mu_xy = mu_x * mu_y;

    theta_x2 = 64 * pre_mu_x2 - mu_x2;
    theta_y2 = 64 * pre_mu_y2 - mu_y2;
    theta_xy = 64 * pre_mu_xy2 - mu_xy;

    return (2 * mu_xy + C1) * (2 * theta_xy + C2) / ((mu_x2 + mu_y2 + C1) * (theta_x2 + theta_y2 + C2));
}

double vp8_ssim(SSIM_CONTEXT* context, const unsigned char *img1, const unsigned char *img2,
    int stride_img1, int stride_img2, int width, int height)
{
    int x, y, x2, y2, img1_block, img2_block, img1_sq_block, img2_sq_block, img12_mul_block, temp;

    double plane_quality, weight, mean;

    short *img1_sum_ptr1, *img1_sum_ptr2;
    short *img2_sum_ptr1, *img2_sum_ptr2;
    int *img1_sq_sum_ptr1, *img1_sq_sum_ptr2;
    int *img2_sq_sum_ptr1, *img2_sq_sum_ptr2;
    int *img12_mul_sum_ptr1, *img12_mul_sum_ptr2;

    plane_quality = 0;

    double plane_summed_weights = (height - 7) * (width - 7);

    //some prologue for the main loop
    temp = 8 * width;

    img1_sum_ptr1      = context->img1_sum + temp;
    img2_sum_ptr1      = context->img2_sum + temp;
    img1_sq_sum_ptr1   = context->img1_sq_sum + temp;
    img2_sq_sum_ptr1   = context->img2_sq_sum + temp;
    img12_mul_sum_ptr1 = context->img12_mul_sum + temp;

    for (x = 0; x < width; x++) {
        context->img1_sum[x]      = img1[x];
        context->img2_sum[x]      = img2[x];
        context->img1_sq_sum[x]   = img1[x] * img1[x];
        context->img2_sq_sum[x]   = img2[x] * img2[x];
        context->img12_mul_sum[x] = img1[x] * img2[x];

        img1_sum_ptr1[x]      = 0;
        img2_sum_ptr1[x]      = 0;
        img1_sq_sum_ptr1[x]   = 0;
        img2_sq_sum_ptr1[x]   = 0;
        img12_mul_sum_ptr1[x] = 0;
    }

    //the main loop
    for (y = 1; y < height; y++) {
        img1 += stride_img1;
        img2 += stride_img2;

        temp = (y - 1) % 9 * width;

        img1_sum_ptr1      = context->img1_sum + temp;
        img2_sum_ptr1      = context->img2_sum + temp;
        img1_sq_sum_ptr1   = context->img1_sq_sum + temp;
        img2_sq_sum_ptr1   = context->img2_sq_sum + temp;
        img12_mul_sum_ptr1 = context->img12_mul_sum + temp;

        temp = y % 9 * width;

        img1_sum_ptr2      = context->img1_sum + temp;
        img2_sum_ptr2      = context->img2_sum + temp;
        img1_sq_sum_ptr2   = context->img1_sq_sum + temp;
        img2_sq_sum_ptr2   = context->img2_sq_sum + temp;
        img12_mul_sum_ptr2 = context->img12_mul_sum + temp;

        for (x = 0; x < width; x++) {
            img1_sum_ptr2[x]      = img1_sum_ptr1[x] + img1[x];
            img2_sum_ptr2[x]      = img2_sum_ptr1[x] + img2[x];
            img1_sq_sum_ptr2[x]   = img1_sq_sum_ptr1[x] + img1[x] * img1[x];
            img2_sq_sum_ptr2[x]   = img2_sq_sum_ptr1[x] + img2[x] * img2[x];
            img12_mul_sum_ptr2[x] = img12_mul_sum_ptr1[x] + img1[x] * img2[x];
        }

        if (y > 6) {
            //calculate the sum of the last 8 lines by subtracting the total sum of 8 lines back from the present sum
            temp = (y + 1) % 9 * width;

            img1_sum_ptr1      = context->img1_sum + temp;
            img2_sum_ptr1      = context->img2_sum + temp;
            img1_sq_sum_ptr1   = context->img1_sq_sum + temp;
            img2_sq_sum_ptr1   = context->img2_sq_sum + temp;
            img12_mul_sum_ptr1 = context->img12_mul_sum + temp;

            for (x = 0; x < width; x++) {
                img1_sum_ptr1[x]      = img1_sum_ptr2[x] - img1_sum_ptr1[x];
                img2_sum_ptr1[x]      = img2_sum_ptr2[x] - img2_sum_ptr1[x];
                img1_sq_sum_ptr1[x]   = img1_sq_sum_ptr2[x] - img1_sq_sum_ptr1[x];
                img2_sq_sum_ptr1[x]   = img2_sq_sum_ptr2[x] - img2_sq_sum_ptr1[x];
                img12_mul_sum_ptr1[x] = img12_mul_sum_ptr2[x] - img12_mul_sum_ptr1[x];
            }

            //here we calculate the sum over the 8x8 block of pixels
            //this is done by sliding a window across the column sums for the last 8 lines
            //each time adding the new column sum, and subtracting the one which fell out of the window
            img1_block      = 0;
            img2_block      = 0;
            img1_sq_block   = 0;
            img2_sq_block   = 0;
            img12_mul_block = 0;

            //prologue, and calculation of simularity measure from the first 8 column sums
            for (x = 0; x < 8; x++) {
                img1_block      += img1_sum_ptr1[x];
                img2_block      += img2_sum_ptr1[x];
                img1_sq_block   += img1_sq_sum_ptr1[x];
                img2_sq_block   += img2_sq_sum_ptr1[x];
                img12_mul_block += img12_mul_sum_ptr1[x];
            }

            double plane = vp8_similarity(img1_block, img2_block, img1_sq_block, img2_sq_block, img12_mul_block);

            plane_quality += plane;

            //and for the rest
            for (x = 8; x < width; x++) {
                img1_block      = img1_block + img1_sum_ptr1[x] - img1_sum_ptr1[x - 8];
                img2_block      = img2_block + img2_sum_ptr1[x] - img2_sum_ptr1[x - 8];
                img1_sq_block   = img1_sq_block + img1_sq_sum_ptr1[x] - img1_sq_sum_ptr1[x - 8];
                img2_sq_block   = img2_sq_block + img2_sq_sum_ptr1[x] - img2_sq_sum_ptr1[x - 8];
                img12_mul_block = img12_mul_block + img12_mul_sum_ptr1[x] - img12_mul_sum_ptr1[x - 8];

                plane = vp8_similarity(img1_block, img2_block, img1_sq_block, img2_sq_block, img12_mul_block);
                plane_quality += plane;
            }
        }
    }

    return plane_quality / plane_summed_weights;
}

double ssim_calc(SSIM_CONTEXT* context, YV12_BUFFER_CONFIG *source, YV12_BUFFER_CONFIG *dest)
{
    double a, b, c;
    double ssimv;

    context->width_y = source->y_width;
    context->height_y = source->y_height;
    context->width_uv = source->uv_width;

    a = vp8_ssim(context, source->y_buffer, dest->y_buffer,
                 source->y_stride, dest->y_stride, source->y_width, source->y_height);

    // In the orginal VP8 source, the luminance informed how much attention to pay
    // to the color, which makes sense for image compression tests.  For browser
    // screenshots I don't think this extra computation is worth it.

    b = vp8_ssim(context, source->u_buffer, dest->u_buffer,
                 source->uv_stride, dest->uv_stride, source->uv_width, source->uv_height);

    c = vp8_ssim(context, source->v_buffer, dest->v_buffer,
                 source->uv_stride, dest->uv_stride, source->uv_width, source->uv_height);

    ssimv = a * .6 + .2 * (b + c);

    return ssimv;
}
