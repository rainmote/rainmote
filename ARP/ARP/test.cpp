#pragma comment(lib, "Ws2_32.lib")
#pragma comment(lib,"Iphlpapi.lib")
#include "arp.h"
#include "SnifferArpResponse.hpp"
#include <iostream>
#include <Windows.h>
#include <WinSock2.h>
#include <Iphlpapi.h>
#include <thread>
#include <vector>
#include <string>

using namespace std;

#define ADAPTER_MAX 10

void mac2String(uint8_t mac[ETH_ALEN], char *str);

bool running = true;

int main()
{
	char mac[ADAPTER_MAX][MAC_BUF_LEN];
	char ip[ADAPTER_MAX][IP_BUF_LEN];
	char gateway[ADAPTER_MAX][IP_BUF_LEN];
	PIP_ADAPTER_INFO pIpAdapterInfo = new IP_ADAPTER_INFO();
	unsigned long stSize = sizeof(IP_ADAPTER_INFO);
	int nRel;
	int netCardNum = 0;
	int IPnumPerNetCard = 0;
	WSADATA wsaData;
	SOCKET arpSocket;
	int seq;
	int choose;

	nRel = GetAdaptersInfo(pIpAdapterInfo, &stSize);
	if (nRel == ERROR_BUFFER_OVERFLOW)
	{
		// Insufficient memory space for pIpAdapterInfo
		delete pIpAdapterInfo;
		pIpAdapterInfo = (PIP_ADAPTER_INFO)malloc(stSize);
		nRel = GetAdaptersInfo(pIpAdapterInfo, &stSize);
	}
	if (ERROR_SUCCESS == nRel)
	{
		seq = 0;
		while (pIpAdapterInfo)
		{
			mac2String(pIpAdapterInfo->Address, mac[seq]);
			cout << "NetCard List:" << endl;
			cout << "[" << seq << "]" << pIpAdapterInfo->Description << "\t";
			cout << "MAC: " << mac[seq] << endl;
			IP_ADDR_STRING *pIpAddrString = &(pIpAdapterInfo->IpAddressList);
			memcpy(ip[seq], pIpAddrString->IpAddress.String, strlen(pIpAddrString->IpAddress.String) + 1);
			memcpy(gateway[seq], pIpAdapterInfo->GatewayList.IpAddress.String, strlen(pIpAdapterInfo->GatewayList.IpAddress.String) + 1);
			do
			{
				cout << "   IP: " << ip[seq] << "\t";
				cout << "Gateway: " << gateway[seq] << endl;
				pIpAddrString = pIpAddrString->Next;
			} while (pIpAddrString);
			pIpAdapterInfo = pIpAdapterInfo->Next;
			seq++;
		}
	}
	cout << "Plase choose[0-" << seq - 1 << "]:";
	cin >> choose;
	//choose = 0;
	if (WSAStartup(MAKEWORD(2, 0), &wsaData) != 0)
	{
		cerr << "Failed to load Winsock." << endl;
		return -1;
	}
	arpSocket = socket(AF_INET, SOCK_RAW, IPPROTO_IP);
	if (arpSocket == SOCKET_ERROR)
	{
		cerr << "Failed to create socket." << endl;
		WSACleanup();
		return -1;
	}

	struct sockaddr_in sendAddr;
	sendAddr.sin_family = AF_INET;

	uint8_t ipIterate[4];
	uint8_t localMac[ETH_ALEN];
	uint8_t localIp[IP_ADDR_LEN];
	struct arp *packet, *recvPacket;
	ARP *arp = new ARP();
	ARP *recvArp = new ARP();
	arp->string2Ip(gateway[choose], ipIterate);
	arp->string2Ip(ip[choose], localIp);
	arp->string2Mac(mac[choose], localMac);

	vector<string> aliveHost;
	for (int i = 170; i < 180; ++i)
	{
		ipIterate[3] = i;
		char ipBuf[IP_BUF_LEN];
		arp->ip2String(ipIterate, ipBuf);
		IPAddr srcIpAddr = inet_addr(ip[choose]);
		IPAddr dstIpAddr = inet_addr(ipBuf);
		ULONG pulMac[2] = {0};
		ULONG ulLen;
		ulLen = sizeof(pulMac);
		memset(&pulMac, 0xff, ulLen);
		HRESULT hr = SendARP(dstIpAddr, srcIpAddr, pulMac, &ulLen);
		if (hr == NO_ERROR)
		{
			aliveHost.push_back(ipBuf);
		}
	}
	cout << "[Alive Host List]" << endl;
	vector<string>::iterator it;
	for (it = aliveHost.begin(); it != aliveHost.end(); it++)
	{
		cout << "\t" << *it << endl;
	}

	if (pIpAdapterInfo)
	{
		delete pIpAdapterInfo;
	}
	delete arp;
	closesocket(arpSocket);
	WSACleanup();
	return 0;
}

void mac2String(uint8_t mac[ETH_ALEN], char *str)
{
	sprintf_s(str, MAC_BUF_LEN, "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
}