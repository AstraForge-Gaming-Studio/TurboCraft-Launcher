/* Host-only regression tests for the native Java Sound OpenAL backend. */
#include <assert.h>
#include <pthread.h>
#include <stdatomic.h>
#include <stdio.h>
#include <time.h>
#include <unistd.h>
#include "DirectAudio.h"

extern char alcMakeContextCurrent(void *context);
extern void *alcGetCurrentContext(void);
extern char alcSetThreadContext(void *context);
extern void *alcGetThreadContext(void);
extern void fakeCaptureAvailable(int frames);

static void *capture;
static _Atomic int done;
static int result;
static void *capture_read(void *unused) {
    (void)unused;
    char data[4] = {0};
    result = DAUDIO_Read(capture, data, sizeof(data));
    if (result == 4) assert(data[0] == 0x5a);
    atomic_store(&done, 1);
    return NULL;
}
static void join_timeout(pthread_t thread) {
    struct timespec deadline;
    clock_gettime(CLOCK_REALTIME, &deadline);
    deadline.tv_sec += 2;
    assert(pthread_timedjoin_np(thread, NULL, &deadline) == 0);
}

int main(void) {
    int global, previous_thread;
    assert(alcMakeContextCurrent(&global));
    assert(alcSetThreadContext(&previous_thread));
    void *playback = DAUDIO_Open(0, 0, 1, DAUDIO_PCM, 48000, 16, 4, 2, 1, 0, 4096);
    assert(playback);
    assert(alcGetCurrentContext() == &global);
    assert(alcGetThreadContext() == &previous_thread);
    char frames[16] = {0};
    assert(DAUDIO_Start(playback, 1));
    assert(DAUDIO_Write(playback, frames, sizeof(frames)) == sizeof(frames));
    assert(alcGetCurrentContext() == &global);
    assert(alcGetThreadContext() == &previous_thread);
    DAUDIO_Close(playback, 1);
    assert(alcGetCurrentContext() == &global);
    assert(alcGetThreadContext() == &previous_thread);

    capture = DAUDIO_Open(0, 0, 0, DAUDIO_PCM, 48000, 16, 4, 2, 1, 0, 4096);
    assert(capture);
    assert(DAUDIO_Start(capture, 0));
    assert(DAUDIO_Read(capture, frames, 1) == 0); /* incomplete frame must not spin */
    pthread_t thread;
    atomic_store(&done, 0);
    assert(pthread_create(&thread, NULL, capture_read, NULL) == 0);
    usleep(50000);
    assert(!atomic_load(&done)); /* running capture waits for data */
    fakeCaptureAvailable(1);
    join_timeout(thread);
    assert(result == 4);

    atomic_store(&done, 0);
    assert(pthread_create(&thread, NULL, capture_read, NULL) == 0);
    usleep(50000);
    assert(!atomic_load(&done));
    assert(DAUDIO_Stop(capture, 0));
    join_timeout(thread);
    assert(result == 0); /* stopping capture unblocks an empty read */
    DAUDIO_Close(capture, 0);
    puts("Native Java Sound host regression tests passed");
    return 0;
}
