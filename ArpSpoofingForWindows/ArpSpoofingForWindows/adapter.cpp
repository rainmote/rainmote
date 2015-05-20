#include "stdafx.h"
#include "ArpSpoofingForWindows.h"
#include "ArpSpoofingForWindowsDlg.h"

#include "adapter.h"


using namespace std;

bool Adapter::running = false;
int Adapter::aliveHostIndex[256] = { 0 };
map<string, string> Adapter::ipMac = map<string, string>();
char Adapter::localIp[IP_BUF_LEN] = "";
char Adapter::localMac[MAC_BUF_LEN] = "";

extern int _log(char *fmt, ...);

Adapter::Adapter()
{
	if (pcap_findalldevs_ex(PCAP_SRC_IF_STRING, NULL, &alldevs, errbuf) == -1)
	{
		return;
	}
	_log("find all adapter dev.\n");
	seq = 0;
	
	for (focusdev = alldevs; focusdev != NULL; focusdev = focusdev->next)
	{
		if (focusdev->name)
		{
			adapterName.push_back(focusdev->name);
		}
		else
		{
			adapterName.push_back("No Name");
		}
		
		if (focusdev->description)
		{
			adapterDescription.push_back(focusdev->description);
		}
		else
		{
			adapterDescription.push_back("No description");
		}

		pcap_addr_t *a;
		char ipBuf[IP_BUF_LEN];
		ip.clear();
		netmask.clear();
		broadaddr.clear();
		for (a = focusdev->addresses; a; a = a->next)
		{
			switch (a->addr->sa_family)
			{
			case AF_INET:
				if (a->addr)
				{
					ARP::ip2String((uint8_t *)&((struct sockaddr_in *)a->addr)->sin_addr.S_un.S_addr, ipBuf);
					ip.push_back(ipBuf);
				}
				if (a->netmask)
				{
					ARP::ip2String((uint8_t *)&(((struct sockaddr_in *)a->netmask)->sin_addr.S_un.S_addr), ipBuf);
					netmask.push_back(ipBuf);
				}
				if (a->broadaddr)
				{
					ARP::ip2String((uint8_t *)&(((struct sockaddr_in *)a->broadaddr)->sin_addr.S_un.S_addr), ipBuf);
					broadaddr.push_back(ipBuf);
				}
				break;
			default:
				break;
			}
		}
		adapterIp.insert(make_pair(focusdev->name, ip));
		adapterNetmask.insert(make_pair(focusdev->name, netmask));
		adapterBroadaddr.insert(make_pair(focusdev->name, broadaddr));
		apapterGateway.insert(make_pair(focusdev->name, gateway));
		seq++;
	}
	running = false;
	adhandle = NULL;
}

Adapter::~Adapter()
{
	pcap_freealldevs(alldevs);
}

int Adapter::openAdapter()
{
	if (adhandle)
	{
		pcap_close(adhandle);
	}
	adhandle = pcap_open(focusdev->name,
		65535,       //存放数据包的内容长度
		PCAP_OPENFLAG_PROMISCUOUS,  //混杂模式
		1000,           //超时时间
		NULL,          //远程验证
		errbuf         //错误缓冲
		);
	if (adhandle == NULL)
	{
		pcap_freealldevs(alldevs); //释放列表
		return -1;
	}
	_log("open focus dev(%s)\n", focusdev->description);

	// 选定只是以太网的网络类型
	if (pcap_datalink(adhandle) != DLT_EN10MB)
	{
		pcap_freealldevs(alldevs); //释放列表
		return -1;
	}
	return 0;
}

void Adapter::setFocusdev(int index)
{
	int i = 0;
	focusdev = alldevs;
	while (i < index)
	{
		focusdev = focusdev->next;
		i++;
	}
	_log("set focus dev(%s)\n", focusdev->description);
}

int Adapter::getFocusdev()
{
	pcap_if_t *d = alldevs;
	int i = 0;
	while (!strcmp(d->name, focusdev->name))
	{
		i++;
		d = d->next;
	}
	return i;
}

void Adapter::sendPacket(const u_char *packet, int len)
{
	int ret = pcap_sendpacket(adhandle, packet, len);
	if (ret)
	{
		_log("send packet error (size:%d)\n", len);
	}
}

void Adapter::startSniffer4AliveHost()
{
	running = true;
	recvthread = CreateThread(NULL, 0, GetAliveHost, adhandle, 0, NULL);
	_log("Create sniffer thread:%d\n", recvthread);
}

void Adapter::stopSniffer4AliveHost()
{
	running = false;
}

int Adapter::getAdapterNum()
{
	return seq;
}

string Adapter::getAdapterName(int index)
{
	return adapterName[index];
}

string Adapter::getAdapterDescription(int index)
{
	return adapterDescription[index];
}

string Adapter::getAdapterMac(int index)
{
	return adapterMac[index];
}

vector<string> Adapter::getAdapterIp(int index)
{
	return adapterIp[getAdapterName(index)];
}

vector<string> Adapter::getAdapterNetmask(int index)
{
	return adapterNetmask[getAdapterName(index)];
}

vector<string> Adapter::getAdapterBroadaddr(int index)
{
	return adapterBroadaddr[getAdapterName(index)];
}

vector<string> Adapter::getAdapterGateway(int index)
{
	return apapterGateway[getAdapterName(index)];
}

DWORD WINAPI Adapter::GetAliveHost(LPVOID lpParameter)
{
	int res;
	pcap_t *adhandle = (pcap_t *)lpParameter;
	struct pcap_pkthdr *pkt_header;
	const u_char *pkt_data;
	ARP *recvArp = new ARP();
	struct arp *packet = recvArp->getPacket();
	memset((char *)aliveHostIndex, 0, 256);
	char buf[IP_BUF_LEN];
	char macBuf[MAC_BUF_LEN];
	while (running)
	{
		if ((res = pcap_next_ex(adhandle, &pkt_header, &pkt_data)) >= 0)
		{
			if (*(unsigned short *)(pkt_data + 12) == htons(ETH_P_ARP) && 
				*(unsigned short *)(pkt_data + 20) == htons(ARP_REPLY))
			{
				memcpy(packet, pkt_data, sizeof(struct arp));
				uint8_t *ip = recvArp->getSrcIpAddr();
				uint8_t *mac = recvArp->getEthernetSrcAddr();
				aliveHostIndex[*(ip + 3) & 0xFF] = 1;
				ARP::ip2String(ip, buf);
				ARP::mac2String(mac, macBuf);
				ipMac.insert(pair<string, string>(buf, macBuf));
			}
		}
	}
	delete recvArp;
	return 0;
}

int Adapter::getSelfMac(int index, char *macBuf)
{
	ARP *arp = new ARP();
	struct pcap_pkthdr *pkt_header;
	const u_char *pkt_data;
	int res;
	char ipBuf[IP_BUF_LEN];
	struct arp *packet = arp->getPacket();
	string str = getAdapterIp(index)[0];
	char *localIp = const_cast<char *>(str.c_str());
	arp->set4LocalHost(localIp);
	u_char buf[128];
	memcpy(buf, arp->getPacket(), sizeof(struct arp));
	if (pcap_sendpacket(adhandle, buf, sizeof(struct arp)))
	{
		_log("Send packet error when getSelfMac().\n");
		return -1;
	}
	bool flag = true;
	int num = 2;
	while (flag && num)
	{
		if ((res = pcap_next_ex(adhandle, &pkt_header, &pkt_data)) == 1)
		{
			if (*(unsigned short *)(pkt_data + 12) == htons(ETH_P_ARP) && 
				*(unsigned short *)(pkt_data + 20) == htons(ARP_REPLY))
			{
				arp->resetPacket();
				memcpy(packet, pkt_data, sizeof(struct arp));
				ARP::ip2String(arp->getSrcIpAddr(), ipBuf);
				_log("LocalIp: %s\n", ipBuf);
				if (!strcmp(ipBuf, localIp))
				{
					ARP::mac2String(arp->getEthernetSrcAddr(), macBuf);
					memcpy(localMac, macBuf, MAC_BUF_LEN);
					_log("LocalMac: %s\n", macBuf);
					flag = false;
				}
			}
		}
		num--;
	}
	delete arp;
	if (!flag)
	{
		return 0;
	}
	return -1;
}