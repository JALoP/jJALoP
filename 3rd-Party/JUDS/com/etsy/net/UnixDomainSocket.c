/***********************************************/
/* UnixDomainSocket.c                          */
/* Inspired by J-BUDS version 1.0              */
/* See COPYRIGHT file for license details      */
/***********************************************/
#include "UnixDomainSocket.h"

#include <jni.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <errno.h>
#include <stdint.h>

#define ASSERTNOERR(cond, msg) do { \
    if (cond) { fprintf(stderr, "[%d] ", errno); perror(msg); return -1; }} while(0)

/* In the class UnixDomainSocket SOCK_DGRAM and SOCK_STREAM correspond to the
 * constant values 0 and 1; SOCK_TYPE replaces them with the respective macro */
#define SOCK_TYPE(type) ((type) == 0 ? SOCK_DGRAM : SOCK_STREAM)


#ifndef SUN_LEN
#define SUN_LEN(su) \
        (sizeof(*(su)) - sizeof((su)->sun_path) + strlen((su)->sun_path))
#endif


socklen_t sockaddr_init(const char* socketFile, struct sockaddr_un* sa) {
    socklen_t salen;

    bzero(sa, sizeof(struct sockaddr_un));
    sa->sun_family = AF_UNIX;
    strcpy(sa->sun_path, socketFile);

    salen = SUN_LEN(sa);
    return salen;
}


JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeCreate(JNIEnv * jEnv,
                            jclass jClass,
                            jstring jSocketFile,
                            jint jSocketType)
{
    int s;            /* socket file handle */
    struct sockaddr_un sa;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);

    socklen_t salen = sockaddr_init(socketFile, &sa);

    /* create the socket */
    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeCreate: socket");

    /* bind to the socket; here the socket file is created */
    ASSERTNOERR(bind(s, (struct sockaddr *)&sa, salen) == -1,
            "nativeCreate: bind");
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM) {
        ASSERTNOERR(listen(s, 0) == -1, "nativeCreate: listen");
        s = accept(s, (struct sockaddr *)&sa, &salen);
        ASSERTNOERR(s == -1, "nativeCreate: accept");
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    /* return the socket file handle */
    return s;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeListen(JNIEnv * jEnv,
                            jclass jClass,
                            jstring jSocketFile,
                            jint jSocketType,
                            jint jBacklog)
{
    int s;            /* socket file handle */
    struct sockaddr_un sa;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);
    socklen_t salen = sockaddr_init(socketFile, &sa);

    /* create the socket */
    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeListen: socket");

    /* bind to the socket; here the socket file is created */
    ASSERTNOERR(bind(s, (struct sockaddr *)&sa, salen) == -1,
            "nativeListen: bind");
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM) {
        ASSERTNOERR(listen(s, jBacklog) == -1, "nativeListen: listen");
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    /* return the listening socket file handle */
    return s;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeAccept(JNIEnv * jEnv,
                            jclass jClass,
                            jint jSocketFileHandle,
                            jint jSocketType)
{
    int s = -1;            /* socket file handle */

    ASSERTNOERR(jSocketFileHandle == -1, "nativeAccept: socket");
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM) {
        s = accept(jSocketFileHandle, NULL, 0);
        ASSERTNOERR(s == -1, "nativeAccept: accept");
    }

    /* return the socket file handle */
    return s;
}


JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeOpen(JNIEnv * jEnv,
                              jclass jClass,
                              jstring jSocketFile,
                              jint jSocketType)
{
    int s;            /* socket file handle */
    struct sockaddr_un sa;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);
    socklen_t salen = sockaddr_init(socketFile, &sa);

    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeOpen: socket");
    if (connect(s, (struct sockaddr *)&sa, salen) == -1) {
	perror("nativeOpen: connect");
	int close_ = close(s);
	ASSERTNOERR(close_ == -1, "nativeOpen: close connect error socket");
	return -1;
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    /* return the socket file handle */
    return s;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeRead(JNIEnv * jEnv,
                              jclass jClass,
                              jint jSocketFileHandle,
                              jbyteArray jbarr,
                              jint off, jint len)
{
    ssize_t count;
    jbyte *cbarr = (*jEnv)->GetByteArrayElements(jEnv, jbarr, NULL);
    ASSERTNOERR(cbarr == NULL, "nativeRead: GetByteArrayElements");

    /* read up to len bytes from the socket into the buffer */
    count = read(jSocketFileHandle, &cbarr[off], len);
    ASSERTNOERR(count == -1, "nativeRead: read");

    (*jEnv)->ReleaseByteArrayElements(jEnv, jbarr, cbarr, 0);

    // end of stream ( 0 in 'C' API should be -1 in java.io.InputStream API )
    if ( count == 0 )
      {
        count = -1;
      }
    /* return the number of bytes read */
    return count;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeWrite(JNIEnv * jEnv,
                               jclass jClass,
                               jint jSocketFileHandle,
                               jbyteArray jbarr,
                               jint off, jint len)
{
    ssize_t count;
    jbyte *cbarr = (*jEnv)->GetByteArrayElements(jEnv, jbarr, NULL);
    ASSERTNOERR(cbarr == NULL, "nativeWrite: GetByteArrayElements");

    /* try to write len bytes from the buffer to the socket */
    count = write(jSocketFileHandle, &cbarr[off], len);
    ASSERTNOERR(count == -1, "nativeWrite: write");

    (*jEnv)->ReleaseByteArrayElements(jEnv, jbarr, cbarr, JNI_ABORT);

    /* return the number of bytes written */
    return count;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeClose(JNIEnv * jEnv,
                               jclass jClass,
                               jint jSocketFileHandle)
{
    return close(jSocketFileHandle);
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeCloseInput(JNIEnv * jEnv,
                                jclass jClass,
                                jint
                                jSocketFileHandle)
{
    /* close the socket input stream */
    return shutdown(jSocketFileHandle, SHUT_RD);
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeCloseOutput(JNIEnv * jEnv,
                                 jclass jClass,
                                 jint
                                 jSocketFileHandle)
{
    /* close the socket output stream */
    return shutdown(jSocketFileHandle, SHUT_WR);
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeUnlink(JNIEnv * jEnv,
                            jclass jClass,
                            jstring jSocketFile)
{
    int ret;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);

    /* unlink socket file */
    ret = unlink(socketFile);

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    return ret;
}

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeSendmsg(JNIEnv * jEnv,
                               jclass jClass,
                               jint jSocketFileHandle,
                               jobject messageHeader)
{

	jclass msghCls = (*jEnv)->GetObjectClass(jEnv, messageHeader);

	// Get the iov from the message header
	jmethodID mGetIov = (*jEnv)->GetMethodID(jEnv, msghCls, "getIov", "()[Ljava/lang/Object;");
	jobjectArray iovArray = (jobjectArray)(*jEnv)->CallObjectMethod(jEnv, messageHeader, mGetIov);

	// Get the iovLen from the iovArray
	int iovLen = (*jEnv)->GetArrayLength(jEnv, iovArray);

	// Create the msghdr struct and fill iov
	struct msghdr msgh;
	memset(&msgh, 0, sizeof(msgh));
	struct iovec iov[iovLen];
	msgh.msg_iovlen = iovLen;
	msgh.msg_iov = iov;

	jclass intClass = (*jEnv)->FindClass(jEnv, "java/lang/Integer");
	jclass shortClass = (*jEnv)->FindClass(jEnv, "java/lang/Short");
	jclass longClass = (*jEnv)->FindClass(jEnv, "java/lang/Long");
	jclass byteArrayClass = (*jEnv)->FindClass(jEnv, "[B");
	jclass stringClass = (*jEnv)->FindClass(jEnv, "java/lang/String");

	int numShorts = 0;
	int numInts = 0;
	int numLongs = 0;
	int numByteArrays = 0;
	int numStrings = 0;

	int i;
	for(i = 0; i < iovLen; i++) {

		jobject obj = (*jEnv)->GetObjectArrayElement(jEnv, iovArray, i);

		if((*jEnv)->IsInstanceOf(jEnv, obj, shortClass) == JNI_TRUE) {
			numShorts++;
		} else if((*jEnv)->IsInstanceOf(jEnv, obj, intClass) == JNI_TRUE) {
			numInts++;
		} else if((*jEnv)->IsInstanceOf(jEnv, obj, longClass) == JNI_TRUE) {
			numLongs++;
		} else if((*jEnv)->IsInstanceOf(jEnv, obj, byteArrayClass) == JNI_TRUE) {
			numByteArrays++;
		} else if((*jEnv)->IsInstanceOf(jEnv, obj, stringClass) == JNI_TRUE) {
			numStrings++;
		}
	}

	uint16_t *shortVals = (uint16_t*) malloc(numShorts * sizeof(uint16_t));
	uint32_t *intVals = (uint32_t*) malloc(numInts * sizeof(uint32_t));
	uint64_t *longVals = (uint64_t*) malloc(numLongs * sizeof(uint64_t));
	uint8_t **byteArrayVals = (uint8_t**) malloc(numByteArrays * sizeof(uint8_t*));
	const char **stringVals = (const char**) malloc(numStrings * sizeof(const char*));
	jobject *objs = (jobject*) malloc(numByteArrays * sizeof(jobject*));

	int shortCounter = 0;
	int intCounter = 0;
	int longCounter = 0;
	int byteArraysCounter = 0;
	int stringCounter = 0;

	for(i = 0; i < iovLen; i++) {

		jobject obj = (*jEnv)->GetObjectArrayElement(jEnv, iovArray, i);

		if((*jEnv)->IsInstanceOf(jEnv, obj, shortClass) == JNI_TRUE) {
			jmethodID shortValueMID = (*jEnv)->GetMethodID(jEnv, shortClass, "shortValue", "()S");
			shortVals[shortCounter] = (uint16_t)(*jEnv)->CallShortMethod(jEnv, obj, shortValueMID);

			iov[i].iov_base = &shortVals[shortCounter];
			iov[i].iov_len = sizeof(shortVals[shortCounter]);

			shortCounter++;

		} else if((*jEnv)->IsInstanceOf(jEnv, obj, intClass) == JNI_TRUE) {
			jmethodID intValueMID = (*jEnv)->GetMethodID(jEnv, intClass, "intValue", "()I");
			intVals[intCounter] = (uint32_t)(*jEnv)->CallIntMethod(jEnv, obj, intValueMID);

			iov[i].iov_base = &intVals[intCounter];
			iov[i].iov_len = sizeof(intVals[intCounter]);

			intCounter++;

		} else if((*jEnv)->IsInstanceOf(jEnv, obj, longClass) == JNI_TRUE) {
			jmethodID longValueMID = (*jEnv)->GetMethodID(jEnv, longClass, "longValue", "()J");
			longVals[longCounter] = (uint64_t)(*jEnv)->CallLongMethod(jEnv, obj, longValueMID);

			iov[i].iov_base = &longVals[longCounter];
			iov[i].iov_len = sizeof(longVals[longCounter]);

			longCounter++;

		} else if((*jEnv)->IsInstanceOf(jEnv, obj, byteArrayClass) == JNI_TRUE) {

			objs[byteArraysCounter] = obj;
			byteArrayVals[byteArraysCounter] = (*jEnv)->GetByteArrayElements(jEnv, obj, NULL);
			int arrayLen = (*jEnv)->GetArrayLength(jEnv, obj);

			iov[i].iov_base = (void*)byteArrayVals[byteArraysCounter];
			iov[i].iov_len = arrayLen;

			byteArraysCounter++;

		} else if((*jEnv)->IsInstanceOf(jEnv, obj, stringClass) == JNI_TRUE) {

			stringVals[stringCounter] = (*jEnv)->GetStringUTFChars(jEnv, obj, NULL);

			iov[i].iov_base = (char*)stringVals[stringCounter];
			iov[i].iov_len = strlen(stringVals[stringCounter]);

			stringCounter++;

		} else {
			// Return -1 because the class type sent isn't handled
			return -1;
		}
	}

	int flags = 0;
	ssize_t bytes_sent = sendmsg(jSocketFileHandle, &msgh, flags);

	ASSERTNOERR(bytes_sent == -1, "nativeSendmsg: sendmsg");

	for(i = 0; i < numByteArrays; i++) {
		(*jEnv)->ReleaseByteArrayElements(jEnv,  objs[i], byteArrayVals[i], JNI_ABORT);
	}

	for(i = 0; i < numStrings; i++) {
		free((char*)stringVals[i]);
	}

	free(shortVals);
	free(intVals);
	free(longVals);
	free(byteArrayVals);
	free(stringVals);
	free(objs);

	// return bytes_sent, will be -1 if there was an error
	return bytes_sent;
}
