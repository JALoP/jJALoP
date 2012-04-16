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

#define JALP_BREAK_STR "BREAK"

JNIEXPORT jint JNICALL
Java_com_etsy_net_UnixDomainSocket_nativeSendmsg(JNIEnv * jEnv,
                               jclass jClass,
                               jint jSocketFileHandle,
                               jbyteArray data,
                               jbyteArray meta,
                               jobject connectionHeader)
{

	jclass connCls = (*jEnv)->GetObjectClass(jEnv, connectionHeader);

	//Get the message type from the connection header
	jmethodID mGetMessageType = (*jEnv)->GetMethodID(jEnv, connCls, "getMessageType", "()I");
	uint16_t messageType = (*jEnv)->CallIntMethod(jEnv, connectionHeader, mGetMessageType);

	//Get the protocol version from the connection header
	jmethodID mGetProtocolVersion = (*jEnv)->GetMethodID(jEnv, connCls, "getProtocolVersion", "()I");
	uint16_t protocolVersion = (*jEnv)->CallIntMethod(jEnv, connectionHeader, mGetProtocolVersion);

	//Get the dataLen from the connection header
	jmethodID mGetDataLen = (*jEnv)->GetMethodID(jEnv, connCls, "getDataLen", "()I");
	uint64_t dataLen = (*jEnv)->CallIntMethod(jEnv, connectionHeader, mGetDataLen);

	//Get the metaLen from the connection header
	jmethodID mGetMetaLen = (*jEnv)->GetMethodID(jEnv, connCls, "getMetaLen", "()I");
	uint64_t metaLen = (*jEnv)->CallIntMethod(jEnv, connectionHeader, mGetMetaLen);

	//Create pointers for the 2 byte arrays, data and meta
	uint8_t *dataPtr = NULL;
	if(data != NULL) {
		dataPtr = (*jEnv)->GetByteArrayElements(jEnv, data, NULL);
	}

	char *metaPtr = NULL;
	if(meta != NULL) {
		metaPtr = (*jEnv)->GetByteArrayElements(jEnv, meta, NULL);
	}

	//Create the msghdr struct and fill iov
	struct msghdr msgh;
	memset(&msgh, 0, sizeof(msgh));
	int iovlen = 8;
	if (messageType == 4) {
		iovlen = 6;
	}
	struct iovec iov[iovlen];
	msgh.msg_iovlen = iovlen;
	msgh.msg_iov = iov;

	int i = 0;
	// protocol version
	iov[i].iov_base = &protocolVersion;
	iov[i].iov_len = sizeof(protocolVersion);
	i++;

	// message type
	iov[i].iov_base = &messageType;
	iov[i].iov_len = sizeof(messageType);
	i++;

	// data length
	iov[i].iov_base = &dataLen;
	iov[i].iov_len = sizeof(dataLen);
	i++;

	// metadata length
	iov[i].iov_base = &metaLen;
	iov[i].iov_len = sizeof(metaLen);
	i++;

	//If message type is not JALP_JOURNAL_FD_MSG set the data
	if (messageType != 4) {
		// log data
		iov[i].iov_base = (void*) dataPtr;
		iov[i].iov_len = dataLen;
		i++;

		// BREAK
		iov[i].iov_base = JALP_BREAK_STR;
		iov[i].iov_len = strlen(JALP_BREAK_STR);
		i++;
	}

	// metadata
	iov[i].iov_base = (void*) metaPtr;
	iov[i].iov_len = metaLen;
	i++;

	// BREAK
	iov[i].iov_base = JALP_BREAK_STR;
	iov[i].iov_len = strlen(JALP_BREAK_STR);

	int flags = 0;
	ssize_t bytes_sent = 0;

	size_t j = 0;
	while (j < (size_t)msgh.msg_iovlen) {
		if (bytes_sent >= (ssize_t)msgh.msg_iov[j].iov_len) {
			bytes_sent -= msgh.msg_iov[j].iov_len;
			msgh.msg_iov[j].iov_len = 0;
			msgh.msg_iov[j].iov_base = NULL;
			j++;
		} else {
			ssize_t offset = msgh.msg_iov[j].iov_len - bytes_sent;
			msgh.msg_iov[j].iov_base += bytes_sent;
			msgh.msg_iov[j].iov_len = offset;
			bytes_sent = sendmsg(jSocketFileHandle, &msgh, flags);

			while (-1 == bytes_sent) {
				int myerrno;
				myerrno = errno;
				if (EINTR == myerrno) {
					bytes_sent = sendmsg(jSocketFileHandle, &msgh, flags);
				} else {
					return -1;
				}
			}
			msgh.msg_control = NULL;
			msgh.msg_controllen = 0;
		}
	}

	ASSERTNOERR(bytes_sent == -1, "nativeSendmsg: sendmsg");

	//If pointers for data and meta exist release them
	if(dataPtr != NULL) {
		(*jEnv)->ReleaseByteArrayElements(jEnv, data, dataPtr, JNI_ABORT);
	}

	if(metaPtr != NULL) {
		(*jEnv)->ReleaseByteArrayElements(jEnv, meta, metaPtr, JNI_ABORT);
	}

	// return bytes_sent, will be -1 if there was an error
	return bytes_sent;
}
