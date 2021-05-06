library(igraph)
library(ggplot2)
adultGraphs <- read.delim("C:/adultGraphs.txt")
if(!is.null(dev.list())) dev.off()
par(mfrow=c(2,2))

for(p in sort(unique(adultGraphs$poison))){
  dat=adultGraphs[adultGraphs$poison==p,c("from","to")]
  gra=graph_from_data_frame(dat)
  #gra<-simplify(gra)
  plot(gra, layout=layout_as_tree,edge.arrow.size=.5,
       vertex.color="gold", vertex.size=15,
       vertex.frame.color="gray",
       main=paste(p,"-level poison, edge count ",ecount(gra)),vertex.label.color="black", vertex.label.cex=0.8, vertex.label.dist=2, edge.curved=0.2)
  
}
deg <- degree(gra, mode="in")

  deg.dist <- degree_distribution(gra, cumulative=T, mode="in")
  plot( x=0:max(deg), y=1-deg.dist, pch=19, cex=1.2,
        col="orange", xlab="Degree", ylab="Cumulative Frequency")

dev.off()
dummy <- read.delim("C:/Users/etr/IdeaProjects/MultiverseJ/dummy.csv", header=FALSE)
colnames(dummy)<-c("treeID","Trees0","Trees45","Votes0","Votes1","y")

reg=0
dummy$homogeneity<-ifelse(dummy$Votes1<=dummy$Votes0,(dummy$Votes0+reg)/(reg+dummy$Votes0+dummy$Votes1),
                     (dummy$Votes1+reg)/(reg+dummy$Votes0+dummy$Votes1))


ggplot(dummy, aes(x = homogeneity)) +
  geom_histogram(aes(y = ..density..)) 
h <- hist(dummy$homogeneity, breaks = 20, plot=FALSE)
h$counts=h$counts/sum(h$counts)
plot(h) 
dummy$clusterSize=dummy$Votes0+dummy$Votes1


ggplot(dummy, aes(x = clusterSize)) +
  geom_histogram(aes(y = ..density..),binwidth=1) 
h <- hist(dummy$clusterSize, breaks = 10, plot=FALSE)
h$counts=h$counts/sum(h$counts)
plot(h) 

dummy$purity<-(dummy$Trees0+reg)/(dummy$Trees0+dummy$Trees45+reg)
ggplot(dummy, aes(x = purity)) +
  geom_histogram(aes(y = ..count..),binwidth=0.05) 
h <- hist(dummy$purity, breaks = 10, plot=FALSE)
h$counts=h$counts/sum(h$counts)
plot(h) 

nrow(dummyThre[dummyThre$y==1,])/nrow(dummyThre)

for(pThreshold in seq(0,1,0.05)){
dummyThre<-dummy[dummy$purity>=pThreshold,]
dummyThre0<-dummyThre[dummyThre$y==0,]
y0_0<-nrow(dummyThre0[dummyThre0$Votes0>=dummyThre0$Votes1,])/nrow(dummyThre)
y0_1<-nrow(dummyThre0[dummyThre0$Votes0<dummyThre0$Votes1,])/nrow(dummyThre)

dummyThre1<-dummyThre[dummyThre$y==1,]
y1_1<-nrow(dummyThre1[dummyThre1$Votes0<=dummyThre1$Votes1,])/nrow(dummyThre)
y1_0<-nrow(dummyThre1[dummyThre1$Votes0>dummyThre1$Votes1,])/nrow(dummyThre)
total=(y0_0+y0_1+y1_1+y1_0)

message(pThreshold,"\t",y0_0,"\t",y0_1,"\t",y1_0,"\t",y1_1,"\t",total)
}




