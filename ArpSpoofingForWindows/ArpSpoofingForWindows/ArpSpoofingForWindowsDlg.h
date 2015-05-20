
// ArpSpoofingForWindowsDlg.h : 头文件
//

#pragma once
#include "afxwin.h"
#include "adapter.h"
#include "arpFrame.h"
#include "afxcmn.h"

#define LOGFILE_ERR		"log.txt"

int _log(char *fmt, ...);

// CArpSpoofingForWindowsDlg 对话框
class CArpSpoofingForWindowsDlg : public CDialogEx
{
// 构造
public:
	CArpSpoofingForWindowsDlg(CWnd* pParent = NULL);	// 标准构造函数
	~CArpSpoofingForWindowsDlg();


// 对话框数据
	enum { IDD = IDD_ARPSPOOFINGFORWINDOWS_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持


// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP();
public:
	CComboBox mAdapter;
	CStatic mAdapterInfo;
	afx_msg void OnCbnSelchangeAdapter();
	Adapter *adapter;
	afx_msg void OnBnClickedButton1();
	CListCtrl mHostList;
	ARP *mArp;
	CListCtrl mAttackList;
	afx_msg void OnBnClickedButton2();
	afx_msg void OnBnClickedButton3();
	CStatic mAttackInfo;
	afx_msg void OnBnClickedButton4();
	char attackIp[IP_BUF_LEN];
	afx_msg void OnBnClickedStart();
	afx_msg void OnBnClickedStop();
};
