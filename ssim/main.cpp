
#include <iostream>

#include "stb/stb_image.h"
#include "ssim.h"
#include "mjpegreader.h"

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <vector>
#include "ssim.h"
#include "stb/stb.h"

static YV12_BUFFER_CONFIG convert(unsigned char* raw, int width, int height, int depth) {
    YV12_BUFFER_CONFIG src;
    src.y_height = height;
    src.y_width = width;
    src.y_stride = width;
    src.uv_height = height;
    src.uv_width = width;
    src.uv_stride = width;
    src.y_buffer = (unsigned char *) malloc((size_t) (height * width));
    src.u_buffer = (unsigned char *) malloc((size_t) (height * width));
    src.v_buffer = (unsigned char *) malloc((size_t) (height * width));

    for (int y = 0; y< height; y++) {
        unsigned char* row1 = raw + (y*width*depth);
        for (int x=0; x<width; x++) {
            src.y_buffer[y*width + x] = row1[x*3 + 0];
            src.u_buffer[y*width + x] = row1[x*3 + 1];
            src.v_buffer[y*width + x] = row1[x*3 + 2];
        }
    }

    return src;
}

static void free_yuv_buffer(YV12_BUFFER_CONFIG* buffer) {
    free(buffer->y_buffer); buffer->y_buffer = 0;
    free(buffer->u_buffer); buffer->u_buffer = 0;
    free(buffer->v_buffer); buffer->v_buffer = 0;
}

static double process_file(SSIM_CONTEXT* context, YV12_BUFFER_CONFIG* dest, unsigned char* raw2, int width, int height, int depth) {
    YV12_BUFFER_CONFIG src = convert(raw2, width, height, depth);

    double weight;
    double p = ssim_calc(context, &src, dest);

    free_yuv_buffer(&src);

    return p;
}

static unsigned char *read_file(QtMjpegContext *c, ml_uint64 offset, ml_uint64 size, unsigned char* buffer) {
    int result = fseeko(c->fp, offset, SEEK_SET);
    if (result == -1) {
        c->eof = 1;
        return nullptr;
    }

    ml_uint64 pos = 0;
    while (true) {
        ml_uint64 n = fread(buffer + pos, 1, size, c->fp);
        pos += n;
        if (pos == size) {
            return buffer;
        }
        if (n == 0) {
            c->eof = 1;
            return nullptr;
        }
    }
}

int main(int argc, char** argv) {
    if (argc != 2) {
        printf("Usage: ssim file.mov\n");
        return 1;
    }

    QtMjpegContext* context = qt_open_mjpeg(argv[1]);

    QtAtom qtAtom;

    while (!context->eof) {
        qtAtom = qt_read_atom(context);

        if (context->eof) {
            break;
        }

        if (qt_cmp_CC4(qtAtom.type, "ftyp")) {
            qt_parse_ftyp(context, qtAtom);
        } else if (qt_cmp_CC4(qtAtom.type, "moov")) {
            qt_parse_moov(context, qtAtom);
        } else if (qt_cmp_CC4(qtAtom.type, "mdat")) {
            qt_parse_mdat(context, qtAtom);
        } else {
            qt_skip_atom(context, qtAtom);
        }
    }

    if (context->mjpeg.video.type == TRACK_TYPE_NONE) {
        printf("File contains no mjpeg video track.\n");
    } else {
        if (context->mjpeg.mdatOffset) {
            QtTrack track = context->mjpeg.video;

            int numChunks = sizeof(track.chunkOffset) / sizeof(*track.chunkOffset);
            if (numChunks == 0) {
                printf("No chunk offsets\n");
                exit(1);
            }

            ml_uint32 timestamp = 0;
            ml_uint64 offset = track.chunkOffset[0];
            ml_uint64 numSamples = track.numSamples;
            ml_uint64* sampleSize = track.sampleSize;
            QtSampleTime* sampleTimes = track.sampleTimes;

            ml_uint64* offsets = new ml_uint64[numSamples];
            ml_uint64* sizes = new ml_uint64[numSamples];
            ml_uint64* timestamps = new ml_uint64[numSamples];

            int i = 0; int c = 0;
            for (int j = 0; j < numSamples; j++) {
                ml_uint64 size = sampleSize[j];
                printf("frame %d offset %llu timestamp: %d\n", j, offset, timestamp);

                offsets[j] = offset;
                timestamps[j] = timestamp;

                timestamp += sampleTimes[i].duration;
                c++;
                if (c >= sampleTimes[i].count) {
                    i++;
                }
                offset += size;
            }

            int w1,w2;
            int h1,h2;
            int c1,c2;

            clock_t start = clock();

            unsigned char* buffer = nullptr;

            SSIM_CONTEXT* ssimContext = ssim_init(4096,4096);

            ml_uint64 bufferSize = sampleSize[numSamples - 1];

            buffer = new unsigned char[bufferSize];
            read_file(context, offsets[numSamples-1], bufferSize, buffer);

            unsigned char* raw1 = stbi_load_from_memory(buffer, bufferSize, &w1, &h1, &c1, STBI_ycbcr);

            YV12_BUFFER_CONFIG dest = convert(raw1, w1, h1, c1);

            double firstValue;

            for (int k = 0; k < numSamples - 1; ++k) {
                ml_uint64 size = sampleSize[k];
                if (size > bufferSize) {
                    delete buffer;
                    buffer = new unsigned char[size];
                    bufferSize = size;
                }
                buffer = read_file(context, offsets[k], size, buffer);
                unsigned char* raw2 = stbi_load_from_memory(buffer, size, &w2, &h2, &c2, STBI_ycbcr);
                double ssim = process_file(ssimContext, &dest, raw2, w1, h1, c1);
                if (k == 0) {
                    firstValue = ssim;
                }
                ssim -= firstValue;
                printf("%llu %f\n", timestamps[k], ssim * (1/ (1-firstValue)));
                stbi_image_free(raw2);
            }

            clock_t end = clock();

            delete buffer;

            stbi_image_free(raw1);

            ssim_destroy(ssimContext);

            printf("Processing time: %lu ms\n", (end - start) / 1000);
        } else {
            printf("File contains no mjpeg video data.\n");
        }
    }

    qt_close_mjpeg(context);
}
