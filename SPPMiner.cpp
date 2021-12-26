// SPBMiner.cpp : Defines the entry point for the console application.
// Author Sajal Halder
// Date 21 December, 2016

#include "stdafx.h"
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<math.h>
#include<vector>
#include<list>
#include<set>
#include<queue>
#include <iomanip>
#include<algorithm>
#include <iterator>
#include <functional>
#include<iostream>
#include<map>
#include <windows.h>
#include <time.h>
#include "windows.h"
#include "psapi.h"
#include <ctime>
#include<limits.h>
#include "tokenizer.h"
#include "allheader.h"
#include "google/sparse_hash_map"
#include "google/dense_hash_map"
#include "google/sparsetable"
#include "MemoryUsage.h"

using namespace std;
using google::sparse_hash_map;
using google::dense_hash_map;
using google::sparsetable;

#define MAX_LINE_LENGTH (8*1024*1024)

//Globals Variable

FILE *fp_in,*fp_out;
char in_file[256] = "";
char out_file[256] = "";


map <int,Entity>supergraph;
vector<int> superentities;
vector<vector<int>> PPatterns; // = new vector<vector<int>>;;
//Graph *SuperGraph;
Patterns *Pattern;
ListHasher *Hash;
int T = 1;
int support[20];
int period[Pmax+1];

void PeriodicEntity(int lavel,int start, int period, int psup,int last);
void EntityUpdate(Entity &E,int time);
void printPattern();
char* vec_to_str(vector<int> &v);
void printHash ();
void write_header(FILE *fp);
void EntityUpdateuncom(Entity &E);	
void printHash(int time);

struct compare  
 {  
   bool operator()(const Descriptor l, const Descriptor r)  
   {  
	   return l.start+l.psup*l.period > r.start+r.period*r.psup;  
   }  
 };  

int _tmain(int argc, _TCHAR* argv[])
{

	
//	LF st=my_clock();
	double _time;
	CHECK_TIME_INIT
	CHECK_TIME_START

	char _outfile[1024]="";
	//sprintf(_outfile,"MemoryProfile%d.txt",j);
	MemoryUsage memory(GetCurrentProcess(),100,_outfile);
	memory.Start();

	int start_s = clock();

	/*for(int i =0; i < 20; i++)
		support [i] = 0;
	for(int i =0; i <=Pmax; i++)
	period[i] = 0;*/
	
	//Assign input dataset 
	//strcpy(in_file,"enron-dated.itemset.txt");
	//strcpy(in_file,"Zigbee_inputexact.txt");
	//strcpy(in_file,"syntheticData_50_2000_15.txt");	
	//strcpy(in_file,"facebook_input.txt");
	strcpy(in_file,"T40I10D100K.txt");
	//strcpy(in_file,"youtube5000.txt");

	//strcpy(in_file,"syntheticData_100_2000_15.txt");
	//strcpy(in_file,"reality_input_2.txt");
//	strcpy(in_file,"sample.txt");

	//strcpy(out_file,"syntheticData_50_2000_15-periodic.txt");
	//strcpy(out_file,"enron-data-periodic.txt");
	//strcpy(out_file,"Zigbee-inoutexact-period.txt");
	//strcpy(out_file,"facebook-inout.txt");
	//strcpy(out_file,"reality_input_2-periodic.txt");
	strcpy(out_file,"T40I10D100K-sup 4-period 10.txt");
	//strcpy(out_file,"youtube5000-sup3_pmax40.txt");

	if(!(fp_in = fopen(in_file, "r")))   // opern input file 
	{
		fprintf(stderr, "%s: cannot open '%s'\n", argv[0], in_file);
		return -2;
	}

	if(!(fp_out = fopen(out_file, "w+")))	{
		fprintf(stderr, " cannot open '%s' for writing\n",  out_file);
		return -2;
	}
	write_header(fp_out);	

	Pattern = new Patterns();                    // pattern dense hash map
    Hash = new ListHasher(true);           // parsimoniou desne hash map
	

	//start reading timesteps from input file and update supergrpah 

	char *buf = new char[MAX_LINE_LENGTH];
	if(!buf) abort();
	Tokenizer tok;

	
	vector<int>::iterator vitc,vits;	
	map<int,Entity>::const_iterator eit;
	vector<int>Diff;
	int total = 0;

	while(fgets(buf,MAX_LINE_LENGTH,fp_in))
	{
		// tokenize the input timestep into an integer set
		tok.tokenize(buf);
		vector<int> current_entities; char *t;
		while((t = tok.token()))	{
			if(t[0] != '*')
				current_entities.push_back(atoi(t));
			total++;
			tok.next();
		}
		sort(current_entities.begin(), current_entities.end());
		current_entities.erase(unique(current_entities.begin(), current_entities.end()), current_entities.end());         // take input current graph
	
		
		// sueprgraph is empty
		if(superentities.size()==0)    //first graph 
		{
			Descriptor D = Descriptor(T,0);			
			for (vitc = current_entities.begin(); vitc!= current_entities.end(); vitc++)

			{

				superentities.push_back(*vitc);		
				Entity E = Entity(*vitc);
				E.TS.push_back(T);				
				supergraph[*vitc] = E;				
			}			
		}
		else // if (current_entities.size() > 0)
		{	
		   vector <int> temp;
		   temp.clear();
		   vitc = current_entities.begin();
		   vits = superentities.begin();

			while(vitc != current_entities.end() && vits != superentities.end())
			{
				eit = supergraph.find(*vits);    
				Entity E = eit->second;
				if(*vitc == *vits)						//find matching 
				{
					temp.push_back(*vits);
					EntityUpdate(E,T);				
					supergraph[*vits] = E;
					vitc++; 
					vits++;					
				}
				else if(*vitc > *vits)				//current entity is higher
				{
					if(T- E.TS.at(E.TS.size()-1)> Pmax)
					{
						supergraph.erase(*vits);					
					}
					else
					{
						temp.push_back(*vits);
						EntityUpdateuncom(E);	
						supergraph[*vits] = E;
					}
					vits++;
				}
				else if(*vits > *vitc)							//super entiy is higher
				{
					temp.push_back(*vitc);
					E = Entity(*vitc);
					E.TS.push_back(T);				
					supergraph[*vitc] = E;
					vitc++;
				}
			}
			
			if(vitc == current_entities.end() && vits == superentities.end())
			{
			}
			else if (vitc == current_entities.end())
			{
				Entity E;
				
				while(vits != superentities.end())
				{
					
					eit = supergraph.find(*vits);
					E = eit->second;
					if(T- E.TS.at(E.TS.size()-1)> Pmax)
					{
						supergraph.erase(*vits);					
					}
					else
					{
						temp.push_back(*vits);
						EntityUpdateuncom(E);	
						supergraph[*vits] = E;
					}
					vits++;
				}
			}
			else
			{
				Entity E;				
				while(vitc != current_entities.end())
				{
					temp.push_back(*vitc);
					E = Entity(*vitc);
					E.TS.push_back(T);				
					supergraph[*vitc] = E;
					vitc++;
				}
			}
			superentities.clear();
			sort(temp.begin(),temp.end());
			copy(temp.begin(),temp.end(),back_inserter(superentities));
			temp.clear();
		//	printf("pattern size = %d\n",Pattern->P2->size());

			printPattern();
			
			Pattern->P2->clear();  // clear current pattern
			//printf("pattern size = %d\n",Pattern->P2->size());
			//Pattern->P2->resize(0);

		//	if(T%(Pmax) ==0)
			//printHash(T);             // print parsimonious pattens

			Diff.clear(); 
		}
		printf("Time = %d\n", T);
		T++;		
	}
	
	lastGraphScan();
	printPattern();
	Pattern->P2->clear();
	//print Hash  
	printHash ();
	Hash->H->clear();	
	
//	LF end=my_clock();

	
	//cout << "time=" << std::setprecision(6) << get_run_time(&end, &st) << "sec" << endl;
	


	CHECK_TIME_END(_time);
	cout << " K time = "<< _time << endl;
	
	fprintf(fp_out,"K time = %f\n", _time);

	cout << "PeakMemory :\t"<< memory.getPeakMemory() << endl;
	fprintf(fp_out,"Peak Memory = %ld \n", memory.getPeakMemory());
	memory.Stop();
	for(int i = 0; i < 20; i++)
	{
		printf(" Supprot = %d   number = %d\n", i,support[i] );
		fprintf(fp_out, " Supprot = %d   number = %d\n", i,support[i]);
	}

	for(int i = 0; i <= Pmax; i++)
	{
		printf(" Period = %d   number = %d\n", i,period[i] );
		fprintf(fp_out, "%d\n", period[i]);
	}
	printf("total = %d",total);
	char ch;
	scanf("%c",&ch);
	return 0;
}

void printHash()
{
	sparse_hash_map<const char*  , vector< Descriptor> ,MurmurHasher<const char*>, eqstr>:: iterator hit;

	for(hit = Hash->H->begin(); hit != Hash->H->end(); hit++)
	{
		const char *key = hit->first;
		vector<Descriptor> D = hit->second;
		vector<Descriptor>::iterator dit;
		for(dit = D.begin(); dit != D.end(); dit++)
		{
			Descriptor preD = *dit;
			
			fprintf(fp_out,"start %d psup %d p %d m %d Pattern = [ ",preD.start,preD.psup,preD.period,preD.start%preD.period);
			
			fprintf(fp_out,key);
			fprintf(fp_out,"]\n");
			for(int i = min_sup; i <=min(preD.psup,19);i++)
			support[i]++;
			period[preD.period]++;
		}		
	}
}

void printHash(int time)
{
	sparse_hash_map<const char*  , vector< Descriptor> ,MurmurHasher<const char*>, eqstr>:: iterator hit;

	vector<Descriptor> UD1;
	vector<const char*> Dkeys;
	Dkeys.clear();
	UD1.clear();
	for(hit = Hash->H->begin(); hit != Hash->H->end(); hit++)
	{
		const char *key = hit->first;
		vector<Descriptor> D = hit->second;
		vector<Descriptor>::iterator dit;
		for(dit = D.begin(); dit != D.end(); dit++)
		{
			Descriptor preD = *dit;
			
			if(preD.start + (preD.psup+1)*preD.period < time)
			{
				fprintf(fp_out,"start %d psup %d p %d m %d Pattern = [ ",preD.start,preD.psup,preD.period,preD.start%preD.period);
				fprintf(fp_out,key);
				fprintf(fp_out,"]\n");

				for(int i = min_sup; i <=min(preD.psup,19);i++)
				support[i]++;
				period[preD.period]++;
			}
			else
			{
				UD1.push_back(preD);
			}	
		}	
		if(UD1.size() ==0)
		{
			Dkeys.push_back(key);			
		}
		else
		{
		Hash->set(key,UD1);
	
		UD1.clear();		
		}

	}
	vector<const char*>::iterator vit;
	for(vit = Dkeys.begin(); vit!= Dkeys.end(); vit++)
	{
		//printf("Delete KKey\n");
		Hash->erase(*vit);
	}
	Hash->H->resize(0);
	Dkeys.clear();
}


void write_header(FILE *fp)	{
	fprintf(fp, "# SPBMiner Implemented By Sajal Halser Copyright (c) 2013\n");
	fprintf(fp, "# Input file :  %s\n", in_file);
	fprintf(fp, "# Output file:  %s\n", out_file);
	fprintf(fp, "# Parameters :  min_sup %d Pmax %d \n", min_sup, Pmax);
}

void EntityUpdate(Entity &E,int time)
{
	
	vector<int>::iterator vit;
	for(vit =E.TS.begin(); vit!=E.TS.end(); vit++)
	{
		if(time-*vit <=Pmax)
		{
			Descriptor D = Descriptor(*vit,time-*vit);   // crate new descripor					    
			E.Descriptors.push(D);
		}
	}
	if(!E.Descriptors.empty())
	{
		Descriptor head = E.Descriptors.top();
		while(!E.Descriptors.empty()&& head.start+head.period*head.psup <= time)
		{
			
			if(head.start+head.period*head.psup == time)
			{
				Descriptor D = head;			
				D.psup++;
				E.Descriptors.push(D);			           
			}

			if(head.psup >= min_sup)
			{
				PeriodicEntity(E.lavel,head.start,head.period,head.psup,head.start+head.period*(head.psup-1));
			}
			E.Descriptors.pop();	
			if(!E.Descriptors.empty())
				head = E.Descriptors.top();
		}		
	}
	if(E.TS.size() == Pmax)
	{
		E.TS.erase(E.TS.begin());
	}
	E.TS.push_back(time);
	//printf("Descriptor Length = %d\n",E.Descriptors.size());
}

void EntityUpdateuncom(Entity &E)
{
	if(E.Descriptors.empty())
		return;
	Descriptor head = E.Descriptors.top();
	while(!E.Descriptors.empty() && head.start+ head.period*head.psup <= T)
	{
		
		if(head.psup >= min_sup)
		{
			PeriodicEntity(E.lavel,head.start,head.period,head.psup,head.start+ head.period*(head.psup-1));
		}
		E.Descriptors.pop();	
			if(!E.Descriptors.empty())
			{
				head = E.Descriptors.top();
			}
		
	}	
	//return E;	
}

// lasl grpah scan 

void lastGraphScan()
{
	map<int,Entity>::iterator mit;
	for(mit = supergraph.begin(); mit!= supergraph.end(); mit++)
	{
		int lavel = mit->first;
		Entity E = mit->second;
		vector<Descriptor> ::iterator dit;
		while(!E.Descriptors.empty())
		{
			Descriptor D = E.Descriptors.top();
			if(D.psup >= min_sup)
			{
				PeriodicEntity(lavel,D.start,D.period,D.psup, D.start + (D.psup-1)*D.period);
			}
			E.Descriptors.pop();

		}		
	}
}
void printVector(vector<int> V)
{
	vector<int>::iterator vit;
		for(vit = V.begin(); vit != V.end(); vit++)
		{
			printf("%d   ",*vit);
		}
		printf("\r\n"); 
}

void printPattern()
{
	sparse_hash_map<const char*, vector<int>, MurmurHasher<const char*>, eqstr>::iterator pit;
	Tokenizer tok2;
	char *buf = new char[40];
	if(Pattern->P2->size() > 0)
	for( pit = Pattern->P2->begin(); pit!= Pattern->P2->end(); pit++)
	{
		const char *key = pit->first;
		vector<int> pattern = pit->second;		
		
		strcpy(buf,key);
	
		int ar [3]; int p = 0;
		char * pch;

		pch = strtok (buf," ,");
		while(pch!=NULL)
		{
			ar [p++] = atoi(pch); 
		
			pch = strtok (NULL," ,");
		}
		int phase = ar[1] % ar[2];
		Descriptor D = Descriptor(ar[0],ar[1],ar[0]%ar[1],ar[0]+ar[1]*(ar[2]-1),ar[2]);
	
		char* parkey = vec_to_str(pattern); 
		
		vector<Descriptor> D2;	
		vector<Descriptor>UD1;

		D2.push_back(D);
		bool sub1,sub2;
		sub1 = false;sub2 = false;
	
		if(Hash->contains(parkey)) 		
		{
			vector<Descriptor> D1 = Hash->get(parkey);
			vector<Descriptor>::iterator dit;
			for( dit = D1.begin(); dit!= D1.end(); dit++)
			{
				Descriptor preD = *dit;
				if((preD.start == D.start && preD.period == D.period && preD.psup < D.psup)|| ((preD.start+(preD.psup-1)*preD.period) == D.start+((D.psup-1)*D.period) && preD.period == D.period && preD.psup < D.psup)&&(preD.period%D.period ==0 && preD.start>=D.start && preD.psup*(preD.psup/D.psup)<=D.psup))
				{	
				}					
				else
				{
					if(preD.start + (preD.psup+1)*preD.period < T)
					{
						fprintf(fp_out,"start %d psup %d p %d m %d Pattern = [ ",preD.start,preD.psup,preD.period,preD.start%preD.period);
						fprintf(fp_out,parkey);
						fprintf(fp_out,"]\n");
					}
					else
					UD1.push_back(preD); 
				}

				if((preD.start == D.start && preD.period == D.period && preD.psup > D.psup)|| ((preD.start+(preD.psup-1)*preD.period) == D.start+((D.psup-1)*D.period) && preD.period == D.period && preD.psup > D.psup)&&(D.period%preD.period ==0 && D.start>=preD.start && D.psup*(D.psup/preD.psup)<=preD.psup))
			
				{
					sub1 = true;
					break;
				}
			}
			if(sub1 == false)
			{
				UD1.push_back(D);
			}
			Hash->set(parkey,UD1);
		}
		else
		{
			Hash->set(parkey, D2);
		}	
		
	}
}

void PeriodicEntity(int lavel,int start, int period, int psup,int last)
{
	int phase = (start)%period;
	
	vector<int> pat;
	pat.push_back(start);
	pat.push_back(period);
	pat.push_back(psup);

	char *key = vec_to_str(pat);

	vector<int> items;

	if(!(Pattern->containsstr(key)))
	{
		items.push_back(lavel);
		Pattern->setstr(key,items);
	}
	else
	{
		items = Pattern->getstr(key);
		items.push_back(lavel);
		sort(items.begin(),items.end());
		items.erase(unique(items.begin(), items.end()), items.end());
		Pattern->setstr(key,items);
	}

	pat.clear();
}

 char *vec_to_str(vector<int> &v)	{
	char *vec_str = NULL;
		unsigned vec_str_alloc;	

	if(!vec_str)	{
		vec_str = new char[1024];
		if(!vec_str) abort();
		vec_str_alloc = 1024;
	}
	*vec_str = 0;
	if(v.size() == 0) 
		return (char *) vec_str;	
	if(vec_str_alloc < v.size()*14)	{
		do {
			vec_str_alloc = vec_str_alloc << 1;
		} while(vec_str_alloc < v.size()*14);
			
		delete[] vec_str;
		vec_str = new char[vec_str_alloc];
		if(!vec_str) abort();
		*vec_str = 0;
	}
	memset(vec_str, 0, vec_str_alloc);
	sprintf(vec_str, "%d", v[0]);
	for(unsigned i = 1; i < v.size(); i++)
		sprintf(strchr(vec_str, 0), " %d", v[i]);
	return (char *) vec_str;
}
