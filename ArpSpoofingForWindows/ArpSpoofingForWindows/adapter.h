#ifndef _ADAPTER_H_
#define _ADAPTER_H_

#include <iostream>
#include <vector>
#include <map>
#include "arpFrame.h"
#include "pcap.h"



using namespace std;

class Adapter
{
public:
	Adapter();
	~Adapter();

	int openAdapter();

	void setFocusdev(int index);
	int getFocusdev();
	int getSelfMac(int index, char *macBuf);

	void sendPacket(const u_char *packet, int len);

	void startSniffer4AliveHost();
	void stopSniffer4AliveHost();

	int getAdapterNum();
	string getAdapterName(int index);
	string getAdapterDescription(int index);
	string getAdapterMac(int index);
	vector<string> getAdapterIp(int index);
	vector<string> getAdapterNetmask(int index);
	vector<string> getAdapterBroadaddr(int index);
	vector<string> getAdapterGateway(int index);

private:
	pcap_if_t *alldevs;
	pcap_if_t *focusdev;
	char errbuf[PCAP_ERRBUF_SIZE];
	pcap_t *adhandle;
	struct pcap_pkthdr *header;
	uint8_t *packetData;

	int seq;
	vector<string> adapterName;
	vector<string> adapterDescription;
	vector<string> adapterMac;
	vector<string> ip;
	vector<string> netmask;
	vector<string> broadaddr;
	vector<string> gateway;
	map<string, vector<string>> adapterIp;
	map<string, vector<string>> adapterNetmask;
	map<string, vector<string>> adapterBroadaddr;
	map<string, vector<string>> apapterGateway;

	HANDLE recvthread;
public:
	static bool running;
	static int aliveHostIndex[256];
	static DWORD WINAPI GetAliveHost(LPVOID lpParameter);
	static map<string, string> ipMac;
	static char localIp[IP_BUF_LEN];
	static char localMac[MAC_BUF_LEN];
};

#endif