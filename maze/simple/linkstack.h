/*Link Stack
 *
 *linkstack.h
 * */

#ifndef _LINKSTACK_H_
#define _LINKSTACK_H_


#include<stdio.h>
#include<stdlib.h>

#define datatype MAZENODE

typedef struct maze
{
	int seq;
	int x;
	int y;
	int di;
}MAZENODE;

typedef struct node
{
	datatype data;
	struct node *next;
}LinkStack;

int StackEmpty(LinkStack *top);

int Push(LinkStack **top, datatype x);

int Pop(LinkStack **top, datatype *x);

#endif

