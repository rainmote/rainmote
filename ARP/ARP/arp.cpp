#include "arp.h"
#include <iostream>

using namespace std;

ARP::ARP()
{
	mArp = (struct arp *)malloc(sizeof(struct arp));
	memset(mArp, 0, sizeof(struct arp));
	// set ethernet frame type
	mArp->ethhdr.h_type = htons(ETH_P_ARP);
	// set hardware type
	mArp->arphdr.hw_type = htons(0x01);
	// set protocol type
	mArp->arphdr.pro_type = htons(0x0800);
	// set hradware&protocol address length
	mArp->arphdr.hw_addr_len = ETH_ALEN;
	mArp->arphdr.pro_addr_len = IP_ADDR_LEN;
	// set op
	mArp->arphdr.op = htons(0x01);
}

ARP::~ARP()
{
	free(mArp);
}

void ARP::setEthernetSrcAddr(uint8_t src[ETH_ALEN])
{
	memcpy(mArp->ethhdr.h_source, src, ETH_ALEN);
}

uint8_t* ARP::getEthernetSrcAddr()
{
	return mArp->ethhdr.h_source;
}

void ARP::setEthernetDstAddr(uint8_t dst[ETH_ALEN])
{
	memcpy(mArp->ethhdr.h_dest, dst, ETH_ALEN);
}

uint8_t* ARP::getEthernetDstAddr()
{
	return mArp->ethhdr.h_dest;
}

void ARP::setSrcIpAddr(uint8_t srcIp[IP_ADDR_LEN])
{
	memcpy(mArp->arphdr.src_ip_addr, srcIp, IP_ADDR_LEN);
}

uint8_t* ARP::getSrcIpAddr()
{
	return mArp->arphdr.src_ip_addr;
}

void ARP::setDstIpAddr(uint8_t dstIp[IP_ADDR_LEN])
{
	memcpy(mArp->arphdr.dst_ip_addr, dstIp, IP_ADDR_LEN);
}

uint8_t* ARP::getDstIpAddr()
{
	return mArp->arphdr.dst_ip_addr;
}

void ARP::setSrcMacAddr(uint8_t srcMac[ETH_ALEN])
{
	memcpy(mArp->arphdr.src_eth_addr, srcMac, ETH_ALEN);
}

uint8_t* ARP::getSrcMacAddr()
{
	return mArp->arphdr.src_eth_addr;
}

void ARP::setDstMacAddr(uint8_t dstMac[ETH_ALEN])
{
	memcpy(mArp->arphdr.dst_eth_addr, dstMac, ETH_ALEN);
}

uint8_t* ARP::getDstMacAddr()
{
	return mArp->arphdr.dst_eth_addr;
}

void ARP::mac2String(uint8_t mac[ETH_ALEN], char *str)
{
	sprintf_s(str, MAC_BUF_LEN, "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
}

void ARP::string2Mac(char *str, uint8_t mac[ETH_ALEN])
{
	for (int i = 0; i < 6; ++i)
	{
		if (isalpha(str[3 * i]))
			mac[i] = (toupper(str[3 * i]) - 'A' + 10) * 16;
		else
			mac[i] = (str[3 * i] - 48) * 16;

		if (isalpha(str[3 * i + 1]))
			mac[i] += toupper(str[3 * i + 1]) - 'A' + 10;
		else
			mac[i] += str[3 * i + 1] - 48;
	}
}

void ARP::ip2String(uint8_t ip[IP_ADDR_LEN], char *str)
{
	sprintf_s(str, IP_BUF_LEN, "%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
}

void ARP::string2Ip(char *str, uint8_t ip[IP_ADDR_LEN])
{
	int i = 0, j = 0;
	for ( ; i < 4; ++i)
	{	
		while (str[j] != '.' && str[j])
		{
			if (j == 0 || str[j - 1] == '.')
			{
				ip[i] = str[j] - 48;
			}
			else
			{
				ip[i] = ip[i] * 10 + str[j] - 48;
			}
			j++;
		}
		j++;
	}
}

void ARP::printMac()
{
	char macSrc[MAC_BUF_LEN], macDst[MAC_BUF_LEN];
	mac2String(mArp->ethhdr.h_source, macSrc);
	mac2String(mArp->ethhdr.h_dest, macDst);
	cout << macSrc << "  ---->  " << macDst << endl;
}

void ARP::printIp()
{
	char ipSrc[IP_BUF_LEN], ipDst[IP_BUF_LEN];
	ip2String(mArp->arphdr.src_ip_addr, ipSrc);
	ip2String(mArp->arphdr.dst_ip_addr, ipDst);
	cout << "\t" << ipSrc << " >> " << ipDst << endl;
}

void ARP::printPacket()
{
	for (int i = 1; i <= sizeof(struct arp); i++) {
		printf("%02x ", *((unsigned char *)mArp + i - 1));
		if (i % 8 == 0) printf("    ");
		if (i % 16 == 0) printf("\n");
	}
	printf("\n");
}

struct arp *ARP::getPacket()
{
	return mArp;
}

void ARP::setBroadcast(uint8_t srcMac[ETH_ALEN], uint8_t srcIp[IP_ADDR_LEN], uint8_t dstIp[IP_ADDR_LEN])
{
	uint8_t broadcastMac[ETH_ALEN] = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };
	setEthernetDstAddr(broadcastMac);
	setEthernetSrcAddr(srcMac);
	setSrcIpAddr(srcIp);
	setDstIpAddr(dstIp);
	setSrcMacAddr(srcMac);
}