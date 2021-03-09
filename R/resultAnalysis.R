library(igraph)

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
  deg <- degree(gra, mode="in")

  deg.dist <- degree_distribution(gra, cumulative=T, mode="in")
  plot( x=0:max(deg), y=1-deg.dist, pch=19, cex=1.2,
        col="orange", xlab="Degree", ylab="Cumulative Frequency")
}
