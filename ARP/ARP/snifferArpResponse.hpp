//#include "arp.h"
//#include <iostream>
//#include <Windows.h>
//#include <vector>
//#include <string>
//#include <thread>
//
//using namespace std;
//
//
//
//
//class SnifferArpResponse
//{
//public:
//	SnifferArpResponse()
//	{
//		running = true;
//	}
//
//
//	void start()
//	{
//	}
//
//	void stop()
//	{
//		running = false;
//	}
//
//	void recvArpResponse()
//	{
//		SOCKET arpSocket = socket(AF_INET, SOCK_RAW, IPPROTO_IP);
//		if (arpSocket == SOCKET_ERROR)
//		{
//			cerr << "Failed to create socket." << endl;
//			WSACleanup();
//			return;
//		}
//
//		vector<string> aliveHost;
//		struct sockaddr_in recvAddr;
//		int len = sizeof(recvAddr);
//		ARP *arp = new ARP();
//		struct arp *packet = arp->getPacket();
//		while (running)
//		{
//			if (recvfrom(arpSocket, (char *)packet, sizeof(struct arp), 0, (struct sockaddr*)&recvAddr, &len) > 0)
//			{
//				char buf[IP_BUF_LEN];
//				arp->ip2String(arp->getSrcIpAddr(), buf);
//				aliveHost.push_back(buf);
//			}
//		}
//
//		cout << "[Alive Host List]" << endl;
//		vector<string>::iterator it;
//		for (it = aliveHost.begin(); it != aliveHost.end(); it++)
//		{
//			cout << "\t" << *it << endl;
//		}
//	}
//
//private:
//	static bool running;
//};
//
