run : maze.o linkstack.o
	gcc -o run maze.o linkstack.o

maze.o : maze.c linkstack.h
	gcc -c maze.c
linkstack.o : linkstack.c linkstack.h
	gcc -c linkstack.c

clean :
	rm maze.o linkstack.o
