#ifndef SSIM_SSIM_H
#define SSIM_SSIM_H

typedef struct {
    int width_y;
    int height_y;
    int width_uv;

    short* img1_sum;
    short* img2_sum;
    int*   img1_sq_sum;
    int*   img2_sq_sum;
    int*   img12_mul_sum;
} SSIM_CONTEXT;

typedef struct
{
    int y_width;
    int y_height;
    int y_stride;

    int uv_width;
    int uv_height;
    int uv_stride;

    unsigned char *y_buffer;
    unsigned char *u_buffer;
    unsigned char *v_buffer;
} YV12_BUFFER_CONFIG;

SSIM_CONTEXT* ssim_init(int maxWidth, int maxHeight);

void ssim_destroy(SSIM_CONTEXT* context);

double ssim_calc(SSIM_CONTEXT *context, YV12_BUFFER_CONFIG *source, YV12_BUFFER_CONFIG *dest);

#endif
