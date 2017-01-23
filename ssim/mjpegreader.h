#include "mlib.h"

#include <time.h>
#include <string.h>

struct QtSampleTime {
    ml_uint32 count;
    ml_uint32 duration;
};

enum QtTrackType {
    TRACK_TYPE_NONE,
    TRACK_TYPE_MJPA,
    TRACK_TYPE_MJPB,
    TRACK_TYPE_JPEG
};

struct QtTrack {
    ml_uint64 *sampleSize;
    ml_uint64 numSamples;
    ml_uint64 *chunkOffset;
    QtSampleTime* sampleTimes;
    int width;
    int height;
    int framesPerSample;
    QtTrackType type = TRACK_TYPE_NONE;
};

struct QtMjpeg {
    ml_uint64 mdatOffset;
    QtTrack video;
    QtTrack track;
};

struct QtMjpegContext {
    FILE* fp;
    int eof;
    ml_uint8* buffer;
    ml_uint8* bufferEnd;
    ml_uint8 bufferStart[128];
    int buflen;
    ml_uint64 bufferFileOffset;
    QtMjpeg mjpeg;
};

struct CC4 {
    union {
        int val;
        char vals[4];
    };

    CC4() : val(0) { }
    CC4(char* in)
    {
        for (int i = 0; i < 4; i++) {
            vals[i] = in[i];
        }
    }
    CC4(ml_uint8 a, ml_uint8 b, ml_uint8 c, ml_uint8 d)
    {
        vals[0] = a;
        vals[1] = b;
        vals[2] = c;
        vals[3] = d;
    }
};

struct QtAtom {
    ml_uint64 offset;
    ml_uint64 size;
    ml_int32 headerSize;
    CC4 type;
};

QtMjpegContext* qt_open_mjpeg(char* file);

void qt_close_mjpeg(QtMjpegContext* context);

QtAtom qt_read_atom(QtMjpegContext* context);

bool qt_cmp_CC4(CC4 atomType, const char *val);

void qt_parse_ftyp(QtMjpegContext *context, QtAtom atom);

void qt_parse_moov(QtMjpegContext *context, QtAtom atom);

void qt_parse_mdat(QtMjpegContext *context, QtAtom atom);

void qt_skip_atom(QtMjpegContext* c, QtAtom a);

#ifdef MQT_IMPLEMENTATION

static void qt_refill_buffer(QtMjpegContext* context) {
    ml_uint64 n = fread(context->bufferStart, 1, context->buflen, context->fp);
    context->bufferFileOffset += n;
    if (n == 0) {
        context->eof = 1;
        context->buffer = context->bufferStart;
        context->bufferEnd = ((ml_uint8*)context->bufferStart)+1;
        *context->buffer = 0;
    } else {
        context->buffer = context->bufferStart;
        context->bufferEnd = context->bufferStart + n;
    }
}

static ml_uint8 qt_get8(QtMjpegContext* context) {
    if (context->buffer < context->bufferEnd)
        return *context->buffer++;
    if (!context->eof) {
        qt_refill_buffer(context);
        return *context->buffer++;
    }
    return 0;
}

static int qt_get16(QtMjpegContext* context) {
    int z = qt_get8(context);
    return (z << 8) + qt_get8(context);
}

static ml_uint32 qt_get32(QtMjpegContext* context) {
    ml_uint32 z = qt_get16(context);
    return (z << 16) + qt_get16(context);
}

static ml_uint64 qt_get64(QtMjpegContext* context) {
    ml_uint64 z = qt_get32(context);
    return (z << 32) + qt_get32(context);
}

static CC4 qt_read_CC4(QtMjpegContext* context) {
    return CC4(qt_get8(context), qt_get8(context), qt_get8(context), qt_get8(context));
}

static ml_uint64 qt_get_offset(QtMjpegContext* context) {
    return context->bufferFileOffset - (context->bufferEnd - context->buffer);
}

QtAtom qt_read_atom(QtMjpegContext* context) {
    QtAtom qtAtom;

    qtAtom.offset = qt_get_offset(context);
    unsigned int size = qt_get32(context);
    qtAtom.type = qt_read_CC4(context);

    if (size == 1) {
        ml_uint64 size64 = qt_get64(context);
        int headerSize = 4 /* size */ + 4 /* type */ + 8 /* 64 bit size */;
        if (size64 > headerSize) {
            qtAtom.size = size64 - headerSize;
        } else {
            qtAtom.size = 0;
        }
        qtAtom.headerSize = headerSize;
    } else {
        int headerSize = 4 /* size */ + 4 /* type */;
        if (size > headerSize) {
            qtAtom.size = size - headerSize;
        } else {
            qtAtom.size = 0;
        }
        qtAtom.headerSize = headerSize;
    }

    return qtAtom;
}

static void qt_skip(QtMjpegContext* c, ml_int64 skipBytes) {
    ml_uint64 r = c->bufferEnd - c->buffer;
    if (skipBytes < r) {
        c->buffer += skipBytes;
    } else {
        int result = fseeko(c->fp, skipBytes - r, SEEK_CUR);
        if (result == -1) {
            c->eof = 1;
        } else {
            c->bufferFileOffset += (skipBytes - r);
        }
        c->buffer = c->bufferEnd;
    }
}

static void qt_skip_atom_remaining(QtMjpegContext* c, QtAtom a) {
    ml_uint64 offset = qt_get_offset(c);
    ml_uint64 newPos = a.offset + a.headerSize + a.size;
    qt_skip(c, (ml_int64)(newPos - offset));
}

static void qt_skip_to(QtMjpegContext* c, ml_uint64 newPos) {
    ml_uint64 offset = qt_get_offset(c);
    qt_skip(c, (ml_int64)(newPos - offset));
}

static const char* qt_container_type_atoms[] = {
    "moov", "trak", "udta", "tref", "imap",
    "mdia", "minf", "stbl", "edts", "mdra",
    "rmra", "imag", "vnrp", "dinf"
};

static int qt_container_type_atoms_len = 14;

static bool qt_is_container_type(CC4 atomType) {
    for (int i = 0; i < qt_container_type_atoms_len; ++i) {
        if (qt_cmp_CC4(atomType, qt_container_type_atoms[i])) {
            return true;
        }
    }

    return false;
}

static void qt_parse_mvhd(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    ml_uint32 creationTime = qt_get32(context);
    ml_uint32 modificationTime = qt_get32(context);
    ml_uint32 timeScale = qt_get32(context);
    ml_uint32 duration = qt_get32(context);
    ml_uint32 preferredRate = qt_get32(context);
    int preferredVolume = qt_get16(context);

    qt_skip(context, 10); // reserved
    qt_skip(context, 36); // matrix stucture

    // The time value in the movie at which the preview begins.
    ml_uint32 previewTime = qt_get32(context);
    // The duration of the movie preview in movie time scale units.
    ml_uint32 previewDuration = qt_get32(context);

    // The time value of the time of the movie poster.
    ml_uint32 posterTime = qt_get32(context);

    // The time value for the start time of the current selection.
    ml_uint32 selectionTime = qt_get32(context);

    // The duration of the current selection in movie time scale units.
    ml_uint32 selectionDuration = qt_get32(context);

    // The time value for current time position within the movie.
    ml_uint32 currentTime =  qt_get32(context);

    // A 32-bit integer that indicates a value to use for the track ID number of the next track added to this movie. Note that 0 is not a valid track ID value.
    ml_uint32 nextTrackId = qt_get32(context);
}

static void qt_parse_tkhd(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    ml_uint32 creationTime = qt_get32(context);
    ml_uint32 modificationTime = qt_get32(context);

    ml_uint32 trackId = qt_get32(context);

    qt_skip(context, 4); // reserved

    ml_uint32 duration = qt_get32(context);

    qt_skip(context, 8); // reserved

    int layer = qt_get16(context);

    int alternateGroup = qt_get16(context);
    int volume = qt_get16(context);

    qt_skip(context, 2); // reserved

    qt_skip(context, 36); // matrix

    ml_uint32 trackWidth = qt_get32(context) >> 16;
    ml_uint32 trackHeight = qt_get32(context) >> 16;

    return;
}

static void qt_parse_hdlr(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    CC4 componentType = qt_read_CC4(context);
    CC4 componentSubType = qt_read_CC4(context);
    CC4 componentManufacturer = qt_read_CC4(context);
    ml_uint32 componentFlags = qt_get32(context);
    ml_uint32 componentFlagMask = qt_get32(context);

    ml_uint8 count = qt_get8(context);

    char* name = new char[count + 1];
    int i = 0;
    for (; i < count; ++i) {
        name[i] = qt_get8(context);
    }
    name[i] = 0;
}

static void qt_parse_stts(QtMjpegContext *context, QtAtom atom) {

    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    // Number of entries
    // A 32-bit integer containing the count of entries in the time-to-sample table.

    ml_uint32 numEntries = qt_get32(context);

    context->mjpeg.track.sampleTimes = new QtSampleTime[numEntries];

    for (int i = 0; i < numEntries; ++i) {
        ml_uint32 sampleCount = qt_get32(context);
        ml_uint32 sampleDuration = qt_get32(context);

        QtSampleTime* sampleTime = context->mjpeg.track.sampleTimes + i;
        sampleTime->duration = sampleDuration;
        sampleTime->count = sampleCount;
    }

    qt_skip_atom_remaining(context, atom);
}

static void qt_parse_stsd(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    ml_uint32 numEntries = qt_get32(context);
    for (int i = 0; i < numEntries; ++i) {
        ml_uint64 offset = qt_get_offset(context);
        ml_uint32 size = qt_get32(context);
        CC4 type = qt_read_CC4(context); // we are looking for mjpa
        qt_skip(context, 6); // reserved
        ml_uint16 dataRef = qt_get16(context);

        // 'mjpa' Motion-JPEG (format A)
        // 'mjpb' Motion-JPEG (format B)

        QtTrackType trackType = TRACK_TYPE_NONE;
        if (qt_cmp_CC4(type, "mjpa")) {
            trackType = TRACK_TYPE_MJPA;
        } else if (qt_cmp_CC4(type, "mjpb")) {
            trackType = TRACK_TYPE_MJPB;
        } else if (qt_cmp_CC4(type, "jpeg")) {
            trackType = TRACK_TYPE_JPEG;
        }

        context->mjpeg.track.type = trackType;

        if (trackType == TRACK_TYPE_MJPA || trackType == TRACK_TYPE_MJPB || trackType == TRACK_TYPE_JPEG) {
            // A 16-bit integer indicating the version number of the compressed data. This is set to 0, unless a compressor has changed its data format.
            int version = qt_get16(context);

            // Revision level
            int revision = qt_get16(context);

            // Vendor
            CC4 vendor = qt_read_CC4(context);

            // A 32-bit integer containing a value from 0 to 1023 indicating the degree of temporal compression.
            ml_uint32 temporalQuality = qt_get32(context);

            // A 32-bit integer containing a value from 0 to 1024 indicating the degree of spatial compression.
            ml_uint32 spatialQuality = qt_get32(context);

            // A 16-bit integer that specifies the width of the source image in pixels.
            context->mjpeg.track.width = qt_get16(context);

            // A 16-bit integer that specifies the height of the source image in pixels.
            context->mjpeg.track.height = qt_get16(context);

            // A 32-bit fixed-point number containing the horizontal resolution of the image in pixels per inch.
            ml_uint32 horizontalResolution = qt_get32(context);

            // A 32-bit fixed-point number containing the vertical resolution of the image in pixels per inch.
            ml_uint32 verticalResolution = qt_get32(context);

            // A 32-bit integer that must be set to 0.
            ml_uint32 dataSize = qt_get32(context);

            // A 16-bit integer that indicates how many frames of compressed data are stored in each sample. Usually set to 1.
            context->mjpeg.track.framesPerSample = qt_get16(context);

            // A 32-byte Pascal string containing the name of the compressor that created the image, such as "jpeg".
            char compressorName[32];
            int len = qt_get8(context);
            int i = 0;
            for (; i < len; ++i) {
                compressorName[i] = qt_get8(context);
            }
            compressorName[i] = 0;

            // A 16-bit integer that indicates the pixel depth of the compressed image. Values of 1, 2, 4, 8 ,16, 24, and 32 indicate the depth of color images. The value 32 should be used only if the image contains an alpha channel. Values of 34, 36, and 40 indicate 2-, 4-, and 8-bit grayscale, respectively, for grayscale images.
            int depth = qt_get16(context);

            //  A 16-bit integer that identifies which color table to use. If this field is set to –1, the default color
            // table should be used for the specified depth. For all depths below 16 bits per pixel, this indicates a
            // standard Macintosh color table for the specified depth. Depths of 16, 24, and 32 have no color table.
            // If the color table ID is set to 0, a color table is contained within the sample description itself.
            // The color table immediately follows the color table ID field in the sample description.
            int colorTableId = qt_get16(context);

            qt_skip_to(context, offset + size);
        }

    }
    qt_skip_atom_remaining(context, atom);
}

static void qt_parse_stsc(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    ml_uint32 numEntries = qt_get32(context);
    for (int i = 0; i < numEntries; ++i) {
        // The first chunk number using this table entry.
        ml_uint32 firstChunk = qt_get32(context);

        // The number of samples in each chunk.
        ml_uint32 samplesPerChunk = qt_get32(context);

        // The identification number associated with the sample description for the sample.
        ml_uint32 sampleDescriptionId =  qt_get32(context);
    }
}

static void qt_parse_stsz(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    // A 32-bit integer specifying the sample size. If all the samples are the same size,
    // this field contains that size value. If this field is set to 0, then the samples
    // have different sizes, and those sizes are stored in the sample size table.
    ml_uint32 commonSampleSize = qt_get32(context);

    ml_uint32 numEntries = qt_get32(context);

    if (numEntries > 0) {
        context->mjpeg.track.sampleSize = new ml_uint64[numEntries];
        context->mjpeg.track.numSamples = numEntries;
        for (int i = 0; i < numEntries; ++i) {
            context->mjpeg.track.sampleSize[i] = qt_get32(context);
        }
    } else {
        context->mjpeg.track.sampleSize = new ml_uint64[1];
        context->mjpeg.track.sampleSize[0] = commonSampleSize;
        context->mjpeg.track.numSamples = 1;
    }
}

static void qt_parse_stco(QtMjpegContext *context, QtAtom atom) {
    ml_uint8 version = qt_get8(context);

    ml_uint8 flags[] = {
            qt_get8(context),
            qt_get8(context),
            qt_get8(context)
    };

    // There is one table entry for each chunk in the media. The offset contains the byte
    // offset from the beginning of the data stream to the chunk. The table is indexed by
    // chunk number—the first table entry corresponds to the first chunk, the second table
    // entry is for the second chunk, and so on.

    ml_uint32 numEntries = qt_get32(context);

    context->mjpeg.track.chunkOffset = new ml_uint64[numEntries];

    for (int i = 0; i < numEntries; ++i) {
        ml_uint32 chunkOffset = qt_get32(context);
        context->mjpeg.track.chunkOffset[i] = chunkOffset;
    }
}

static void qt_parse_stbl(QtMjpegContext *context, QtAtom atom) {
    QtAtom child;

    ml_uint64 remain = atom.size;

    while (remain) {
        child = qt_read_atom(context);

        if (context->eof) {
            return;
        }

        if (qt_cmp_CC4(child.type, "stsd")) {
            qt_parse_stsd(context, child);
        } else  if (qt_cmp_CC4(child.type, "stts")) {
            qt_parse_stts(context, child);
        } else  if (qt_cmp_CC4(child.type, "stsc")) {
            qt_parse_stsc(context, child);
        } else  if (qt_cmp_CC4(child.type, "stsz")) {
            qt_parse_stsz(context, child);
        } else  if (qt_cmp_CC4(child.type, "stco")) {
            qt_parse_stco(context, child);
        } else {
            qt_skip_atom(context, child);
        }

        remain -= child.size + atom.headerSize;
    }
}

static void qt_parse_minf(QtMjpegContext *context, QtAtom atom) {
    QtAtom child;

    ml_uint64 remain = atom.size;

    while (remain) {
        child = qt_read_atom(context);

        if (context->eof) {
            return;
        }

        if (qt_cmp_CC4(child.type, "hdlr")) {
            qt_parse_hdlr(context, child);
        } else if (qt_cmp_CC4(child.type, "minf")) {
            qt_parse_minf(context, child);
        } else if (qt_cmp_CC4(child.type, "stbl")) {
            qt_parse_stbl(context, child);
        } else {
            qt_skip_atom(context, child);
        }

        remain -= child.size + atom.headerSize;
    }
}

static void qt_parse_mdia(QtMjpegContext *context, QtAtom atom) {
    QtAtom child;

    ml_uint64 remain = atom.size;

    while (remain) {
        child = qt_read_atom(context);

        if (context->eof) {
            return;
        }

        if (qt_cmp_CC4(child.type, "hdlr")) {
            qt_parse_hdlr(context, child);
        } else if (qt_cmp_CC4(child.type, "minf")) {
            qt_parse_minf(context, child);
        } else {
            qt_skip_atom(context, child);
        }

        remain -= child.size + atom.headerSize;
    }
}

static void qt_parse_trak(QtMjpegContext *context, QtAtom atom) {
    QtAtom child;

    ml_uint64 remain = atom.size;

    while (remain) {
        child = qt_read_atom(context);

        if (context->eof) {
            return;
        }

        if (qt_cmp_CC4(child.type, "tkhd")) {
            qt_parse_tkhd(context, child);
        } else if (qt_cmp_CC4(child.type, "mdia")) {
            qt_parse_mdia(context, child);
        } else {
            qt_skip_atom(context, child);
        }

        remain -= child.size + atom.headerSize;
    }

    if (context->mjpeg.track.type == TRACK_TYPE_MJPA || context->mjpeg.track.type == TRACK_TYPE_MJPB ||
            context->mjpeg.track.type == TRACK_TYPE_JPEG) {
        context->mjpeg.video = context->mjpeg.track;
    }
}

QtMjpegContext* qt_open_mjpeg(char* file) {
    QtMjpegContext *context = new QtMjpegContext();
    context->fp = ml_fopen(file, "rb");
    context->buffer = context->bufferStart;
    context->buflen = sizeof(context->bufferStart);
    context->eof = 0;
    context->bufferFileOffset = 0;

    return context;
}

void qt_close_mjpeg(QtMjpegContext* context) {
    if (context->fp) {
        fclose(context->fp);
    }

    delete context;
}

bool qt_cmp_CC4(CC4 atomType, const char *val) {
    for (int i = 0; i < 4; ++i) {
        if (atomType.vals[i] != val[i]) {
            return false;
        }
    }
    return true;
}

void qt_parse_ftyp(QtMjpegContext *context, QtAtom atom) {
    CC4 brand = qt_read_CC4(context);

    if (!qt_cmp_CC4(brand, "qt  ")) {
        context->eof = 1;
        return;
    }

    ml_uint32 minor_version = qt_get32(context);

    int compatible_brands = (int) ((atom.size - 8) / 4);
    bool isQuickTimeFile = false;
    for (int i = 0; i < compatible_brands; ++i) {
        CC4 brand = qt_read_CC4(context);
        if (qt_cmp_CC4(brand, "qt  ")) {
            isQuickTimeFile = true;
        }
    }

    if (!isQuickTimeFile) {
        context->eof = 1;
        return;
    }
}

void qt_parse_moov(QtMjpegContext *context, QtAtom atom) {
    QtAtom child;

    ml_uint64 remain = atom.size;

    while (remain) {
        child = qt_read_atom(context);

        if (context->eof) {
            return;
        }

        if (qt_cmp_CC4(child.type, "mvhd")) {
            qt_parse_mvhd(context, child);
        } else if (qt_cmp_CC4(child.type, "trak")) {
            qt_parse_trak(context, child);
        } else {
            qt_skip_atom(context, child);
        }

        remain -= child.size + atom.headerSize;
    }
}

void qt_parse_mdat(QtMjpegContext *context, QtAtom atom) {
    context->mjpeg.mdatOffset = ftello(context->fp);
    qt_skip_atom(context, atom);
}

void qt_skip_atom(QtMjpegContext* c, QtAtom a) {
    qt_skip(c, a.size);
}

#endif
