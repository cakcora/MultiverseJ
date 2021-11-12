require(plyr)
require(ggplot2)
projectPath<-"C:/Users/cakcora/IdeaProjects/multiverseJ/"
#"Bank-note","mushroom"
datasets<-c("adult", "LR", "Poker","Nursery","connect-4","Breast-cancer", 
            "Diabetes","Mushroom","spambase","credit"
               "News-popularity")


allData<-data.frame()
poison1=0
maxTrees<-100
for(dataset in datasets){
  maxVanillaAUC=0
  maxVanillaLevel=""
  maxMultiverseAUC=0
  maxMultiverseLevel=""
  for(poison2 in c(0,2,4,6,8,10,20,40)){
    resultsPath = paste0(projectPath, "results/",poison1,"_",poison2,"/")
    vanillafile<-paste0(resultsPath,dataset,"finalResults.txt")
    if(!file.exists(vanillafile)) next;
    multiverseAUC<-read.delim(vanillafile), header=FALSE)
    vanillaAUC<-read.delim(paste0(resultsPath,dataset,"VanillaAucOnTestData.txt"), header=FALSE)
    colnames(vanillaAUC)<-c('dataset','poisonLevel','AUC','bias','loss','time')
    colnames(multiverseAUC)<-c('method','k','kActual','AUC','bias','loss','numTree')
    vanillaAUC$AUC<-round(vanillaAUC$AUC,3)
    multiverseAUC$AUC<-round(multiverseAUC$AUC,3)
    multiverseAUC<-multiverseAUC[multiverseAUC$numTree<maxTrees,]
    vanillaAUC$numTree<-300
    vanillaAUC$poisonLevel<-NULL
    vanillaAUC$time<-NULL
    mx=max(vanillaAUC$AUC)
    if(maxVanillaAUC<mx){
      maxVanillaAUC=mx
      maxVanillaLevel=paste0(poison1,"_",poison2)
    }
    multiverseAUC$kActual<-NULL
    multiverseAUC$method <- substr( multiverseAUC$method,0,7)
    mx=max(multiverseAUC$AUC)
    if(maxMultiverseAUC<mx){
      maxMultiverseAUC=mx
      wh<-multiverseAUC[multiverseAUC$AUC==mx,]
      multiverseMethod = paste(wh$method, collapse=', ' )
      multiverseK = paste(wh$numTree, collapse=', ' )
      maxMultiverseLevel=paste0(poison1,"_",poison2)
    }
    sumRes<-ddply(multiverseAUC, .(k,method), summarize,
                  meanAUC = round(mean(AUC), 3), sdAUC = round(sd(AUC), 2),
                  meanBias = round(mean(bias), 2),sdBias = round(sd(bias), 2),
                  meanLoss = round(mean(loss), 2),sdLoss = round(sd(loss), 2),nTree=mean(numTree))
    #show(dataset)
    
    #show(sumRes)
    
    sumRes2<-ddply(vanillaAUC, .(numTree), summarize,
                   meanAUC = round(mean(AUC), 3), sdAUC = round(sd(AUC), 2),
                   meanBias = round(mean(bias), 2),sdBias = round(sd(bias), 2),
                   meanLoss = round(mean(loss), 2),sdLoss = round(sd(loss), 2))
    #show(sumRes2)
    ggplot(sumRes,aes(x=k,y=meanAUC,group=method,color=method))+geom_line()
  }
  message(dataset," ",maxVanillaAUC," ",maxMultiverseAUC," ",maxMultiverseLevel,"(",multiverseMethod,",",multiverseK,")")
}


for(dataset in datasets){
  multiverseOutput<-read.delim(paste0(resultsPath,dataset,"clusteroutput.txt"), header=FALSE)
  colnames(multiverseOutput)<-c('cluster','phase','datapointID','poison2trees','poison1trees','yhat','y')
  sumRes3<-ddply(multiverseOutput, .(cluster), summarize,
                 numTrees = mean(poison1trees))
  hist(sumRes3$numTrees)
}
