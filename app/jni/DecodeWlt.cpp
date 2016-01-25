#include "DecodeWlt.h"
#include <dlfcn.h>
#include <stdio.h>
#include <malloc.h>
#include <stdlib.h>

#ifndef LOG_TAG
#define LOG_TAG "MY_DEFAULT"
#endif
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define TRUE 1
#define FALSE 0

extern "C" int unpack(const char* pszInFile, const char* pszBmpPath, int bSaveBmp);
typedef int (*def_unpack)(const char*, const char*, int);
int unpack(char *src,char *dst,int bmpSave);

typedef struct tagRGBQUAD { // 定义每个像素的数据类型
	unsigned char rgbBlue;
	unsigned char rgbGreen;
	unsigned char rgbRed;
} RGBQUAD;

#pragma pack(1)
typedef struct tagBITMAPFILEHEADER {	// bmfh
	unsigned short	bfType;			// 文件类型 "BM"
	unsigned long	bfSize;			// 文件大小 38862
	unsigned short	bfReserved1;	// 保留,必须为0
	unsigned short	bfReserved2;	// 保留,必须为0
	unsigned long	bfOffBits;		// 从文件头到位图信息的长度,即BMP总文件头的长度 54
} BITMAPFILEHEADER;
#pragma pack()

typedef struct tagBITMAPINFOHEADER {   /*   bmih   */   
	unsigned long	biSize;				// 本结构体大小	 40
	long			biWidth;			// 图橡的宽度,以像素为单位 102
	long			biHeight;			// 图像的高度,以像素为单位 正值为倒向, 负值为正向 126
	unsigned short	biPlanes;			// 目标设备说明位面数,总为1
	unsigned short	biBitCount;			// 位数/像素,其值为24 ( 1,4,8,16,24或32)
	unsigned long	biCompression;		// 数据压值的类型 BI_RGB:无压缩 BI_RLE8:每个像素8bit的RLE压缩编码,压缩格式由2字节组成  BI_ RLE4:每个像素4bit的压缩编码 BI_BITFIELDS:每个像素的bit由掩码决定
	unsigned long	biSizeImage;		// 图像的大小, 以字节为单位, BI_RGB格式时为0
	long			biXPelsPerMeter;	// 水平分辨率, 以像素/米表示
	long			biYPelsPerMeter;	// 垂直分辨率, 以像素/米表示
	unsigned long	biClrUsed;			// 说明位图实际使用的彩色表中的颜色索引数
	unsigned long	biClrImportant;		// 说明对图象显示有重要影响的颜色索引的数目，如果是0，表示都重要
} BITMAPINFOHEADER;

typedef struct tagBITMAPINFO {	// bmi
	BITMAPINFOHEADER	bmiHeader;
	RGBQUAD				bmiColor[1];
} BITMAPINFO;



JNIEXPORT jint JNICALL Java_com_synjones_bluetooth_DecodeWlt_Wlt2Bmp
  (JNIEnv * env, jobject obj, jstring wltPath, jstring bmpPath)
{
	const char* pszWltPath=NULL;
	const char* pszBmpPath=NULL;
	jboolean isCopy=JNI_FALSE;
	jint result;
	const int xsize = 102;
	const int ysize = 126;
	int i,j;
	//static int testindex = 1;
	//char * testptr = NULL;


	BITMAPFILEHEADER bmfHeader;
	BITMAPINFOHEADER bmiHeader;
	RGBQUAD rgbQuad;

	printf("DecodeWlt go to here start ");
	if(NULL==wltPath || NULL==bmpPath)
		return -11;
	do
	{
		pszWltPath=env->GetStringUTFChars(wltPath,&isCopy);
		if(NULL==pszWltPath)
		{
			result=-14;
			break;
		}
		pszBmpPath=env->GetStringUTFChars(bmpPath,&isCopy);
		if(NULL==pszBmpPath)
		{
			result=-14;
			break;
		}
		LOGE("begin read wlt file.");

		// 读入wlt

		char * wlt = (char *)malloc(1024);
		char * bmp = (char *)malloc(xsize*ysize*3);
		FILE * fp = fopen(pszWltPath, "r+b");
		if (fp != NULL)
		{
			fread(wlt, sizeof(char), 1024, fp);
			fclose(fp);
		}
		else
		{
			result = -15;
			free(wlt);
			free(bmp);
			break;
		}
		LOGE("read wlt file end.");
		result=unpack(wlt,bmp,0);
		//result = 1;
//		testindex++;
//		if (testindex>=3)
//		{
//			testptr = (char*)0xfefefefc;
//			LOGE("Fatal test begin!testindex=%d",testindex);
//			memset(testptr, 0x00, 10);
//		}
//		result = 1;
		LOGE("unpack complete! 2012 09 18");
		if (result == 1)
		{
			bmfHeader.bfType = 0x4D42;
			bmfHeader.bfSize = (102 * 3 + 2) * 126 + 54;
			bmfHeader.bfReserved1 = 0x0000;
			bmfHeader.bfReserved2 = 0x0000;
			bmfHeader.bfOffBits = 54;

			bmiHeader.biSize = 40;
			bmiHeader.biWidth = 102;
			bmiHeader.biHeight = 126;
			bmiHeader.biPlanes = 1;
			bmiHeader.biBitCount = 24;
			bmiHeader.biCompression = 0;
			bmiHeader.biSizeImage = 0;
			bmiHeader.biXPelsPerMeter = 0;
			bmiHeader.biYPelsPerMeter = 0;
			bmiHeader.biClrUsed = 0;
			bmiHeader.biClrImportant = 0;

			rgbQuad.rgbBlue = 0x00;//0xCE;
			rgbQuad.rgbGreen = 0x00;
			rgbQuad.rgbRed = 0x00;

			//fp = fopen("/mnt/sdcard/photo.bmp", "w+b");
			fp = fopen(pszBmpPath, "w+b");
			if (fp != NULL)
			{
				fwrite(&bmfHeader, 14, 1, fp);
				fwrite(&bmiHeader, 40, 1, fp);
				for (j=0;j<ysize;j++)
				{
					for (i=0;i<xsize;i++)
					{
						fwrite(bmp+j*xsize*3+i*3+2, 1, 1, fp);
						fwrite(bmp+j*xsize*3+i*3+1, 1, 1, fp);
						fwrite(bmp+j*xsize*3+i*3+0, 1, 1, fp);
					}
					fwrite(&rgbQuad, 1, 2, fp);
				}
				fclose(fp);
				LOGE("write bmp file %s", pszBmpPath);
			}
		}
		free(wlt);
		free(bmp);
	}while(FALSE);
	if(NULL!=pszWltPath)
		env->ReleaseStringUTFChars(wltPath,pszWltPath);
	if(NULL!=pszBmpPath)
		env->ReleaseStringUTFChars(bmpPath,pszBmpPath);
	return result;
}
