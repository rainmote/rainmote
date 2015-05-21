/*
 *linkstack.c
 *
 * */
#include "linkstack.h"

int StackEmpty(LinkStack *top)
{
	return(top ? 0 : 1);
}

int Push(LinkStack **top, datatype x)
{
	LinkStack *p;
	p = (LinkStack *)malloc(sizeof(LinkStack));
	p->data = x;
	p->next = *top;
	*top = p;
	return 1;
}

int Pop(LinkStack **top, datatype *x)
{
	LinkStack *p;
	if(StackEmpty(*top))
	{
		printf("Stack is Empty!\n");
		return 0;
	}
	*x = (*top)->data;
	p = *top;
	*top = (*top)->next;
	free(p);
	return 1;
}
