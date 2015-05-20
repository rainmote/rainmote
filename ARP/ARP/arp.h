#ifndef _ARP_H_
#define _ARP_H_

#include <WinSock2.h>

typedef unsigned char	uint8_t;
typedef unsigned short	uint16_t;
typedef unsigned int	uint32_t;
//typedef unsigned long	uint64_t;

#define ETH_P_ARP 0x0806

#define ETH_ALEN		6		// Octets in one ethernet addr
#define ETH_HLEN		14		// Total octets in header
#define ETH_ZLEN		60		// Min. octets in frame sans FCS
#define ETH_DATA_LEN	1500	// Max. octets in payload
#define ETH_FRAME_LEN	1514	// Max. octets in frame sans FCS
#define ETH_FCS_LEN		4		// Octets in the FCS
#define IP_ADDR_LEN		4

#define MAC_BUF_LEN 18
#define IP_BUF_LEN	16

#pragma pack(push,1)
struct eth_header
{
	uint8_t h_dest[ETH_ALEN];
	uint8_t h_source[ETH_ALEN];
	uint16_t h_type;
};

struct eth_arp
{
	uint16_t hw_type; // hardware type
	uint16_t pro_type; // protocol type
	uint8_t hw_addr_len; // hardware address length
	uint8_t pro_addr_len; // protocol address length
	uint16_t op; // operation field
	uint8_t src_eth_addr[ETH_ALEN];
	uint8_t src_ip_addr[IP_ADDR_LEN];
	uint8_t dst_eth_addr[ETH_ALEN];
	uint8_t dst_ip_addr[IP_ADDR_LEN];
};

struct arp
{
	struct eth_header ethhdr;
	struct eth_arp arphdr;
	uint8_t padding[18]; 
	// 64 - FCS - sizeof(struct eth_header) - sizeof(struct eth_arp)
};
#pragma pack(pop)

class ARP
{
public:
	ARP();
	~ARP();
	void setEthernetSrcAddr(uint8_t src[ETH_ALEN]);
	uint8_t* getEthernetSrcAddr();
	void setEthernetDstAddr(uint8_t dst[ETH_ALEN]);
	uint8_t* getEthernetDstAddr();

	void setSrcIpAddr(uint8_t srcIp[IP_ADDR_LEN]);
	uint8_t* getSrcIpAddr();
	void setDstIpAddr(uint8_t dstIp[IP_ADDR_LEN]);
	uint8_t* getDstIpAddr();

	void setSrcMacAddr(uint8_t srcMac[ETH_ALEN]);
	uint8_t* getSrcMacAddr();
	void setDstMacAddr(uint8_t dstMac[ETH_ALEN]);
	uint8_t* getDstMacAddr();

	void mac2String(uint8_t mac[ETH_ALEN], char *str);
	void string2Mac(char *str, uint8_t mac[ETH_ALEN]);
	void ip2String(uint8_t ip[IP_ADDR_LEN], char *str);
	void string2Ip(char *str, uint8_t ip[IP_ADDR_LEN]);
	void printMac();
	void printIp();
	void printPacket();

	struct arp *getPacket();
	void setBroadcast(uint8_t srcMac[ETH_ALEN], uint8_t srcIp[IP_ADDR_LEN], uint8_t dstIp[IP_ADDR_LEN]);

private:
	struct arp *mArp;
	char macStringBuf[MAC_BUF_LEN];
	uint8_t macAddrBuf[ETH_ALEN];
	char ipStringBuf[IP_BUF_LEN];
	uint8_t ipAddrBuf[IP_ADDR_LEN];
};

#endif