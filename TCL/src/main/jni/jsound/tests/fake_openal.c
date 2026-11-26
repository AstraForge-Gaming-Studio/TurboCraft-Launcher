/* Host-only OpenAL stand-in for Java Sound regression tests; not packaged. */
#include <stdatomic.h>
#include <string.h>

static int device, bridge_context, capture_device;
static void *global_context;
static _Thread_local void *thread_context;
static _Atomic int available;

void *alcOpenDevice(const char *name) { (void)name; return &device; }
char alcCloseDevice(void *d) { (void)d; return 1; }
void *alcCreateContext(void *d, const int *attr) { (void)d; (void)attr; return &bridge_context; }
void alcDestroyContext(void *c) { (void)c; }
char alcMakeContextCurrent(void *c) { global_context = c; return 1; }
void *alcGetCurrentContext(void) { return global_context; }
char alcSetThreadContext(void *c) { thread_context = c; return 1; }
void *alcGetThreadContext(void) { return thread_context; }
void *alcCaptureOpenDevice(const char *name, unsigned rate, int format, int frames) {
    (void)name; (void)rate; (void)format; (void)frames; return &capture_device;
}
char alcCaptureCloseDevice(void *d) { (void)d; return 1; }
void alcCaptureStart(void *d) { (void)d; }
void alcCaptureStop(void *d) { (void)d; }
void fakeCaptureAvailable(int frames) { atomic_store(&available, frames); }
void alcGetIntegerv(void *d, int param, int size, int *out) {
    (void)d; (void)param; (void)size; *out = atomic_load(&available);
}
void alcCaptureSamples(void *d, void *buffer, int frames) {
    (void)d; memset(buffer, 0x5a, (size_t)frames * 4); atomic_fetch_sub(&available, frames);
}
void alGenSources(int n, unsigned *ids) { while (n--) *ids++ = 7; }
void alDeleteSources(int n, const unsigned *ids) { (void)n; (void)ids; }
void alGenBuffers(int n, unsigned *ids) { for (int i=0; i<n; i++) ids[i] = (unsigned)i+1; }
void alDeleteBuffers(int n, const unsigned *ids) { (void)n; (void)ids; }
void alBufferData(unsigned id, int format, const void *data, int size, int rate) {
    (void)id; (void)format; (void)data; (void)size; (void)rate;
}
void alSourceQueueBuffers(unsigned id, int count, const unsigned *buffers) { (void)id; (void)count; (void)buffers; }
void alSourceUnqueueBuffers(unsigned id, int count, unsigned *buffers) { (void)id; (void)count; *buffers = 1; }
void alSourcePlay(unsigned id) { (void)id; }
void alSourceStop(unsigned id) { (void)id; }
void alSourcei(unsigned id, int param, int value) { (void)id; (void)param; (void)value; }
void alGetSourcei(unsigned id, int param, int *value) { (void)id; (void)param; *value = 0; }
