
// ArpSpoofingForWindows.h : PROJECT_NAME Ӧ�ó������ͷ�ļ�
//

#pragma once

#ifndef __AFXWIN_H__
	#error "�ڰ������ļ�֮ǰ������stdafx.h�������� PCH �ļ�"
#endif

#include "resource.h"		// ������


// CArpSpoofingForWindowsApp: 
// �йش����ʵ�֣������ ArpSpoofingForWindows.cpp
//

class CArpSpoofingForWindowsApp : public CWinApp
{
public:
	CArpSpoofingForWindowsApp();

// ��д
public:
	virtual BOOL InitInstance();

// ʵ��

	DECLARE_MESSAGE_MAP()
};

extern CArpSpoofingForWindowsApp theApp;