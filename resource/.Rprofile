options(repos="http://mirrors.tuna.tsinghua.edu.cn/CRAN/")

.First <- function(){
	mm <- 3
}

readQu <- function(file){
	raw <- read.table(file, header=TRUE, sep = ",", fileEncoding="GBK")
	names(raw) <- 1:length(names(raw))
	sel <- raw[, c(4, 13, 14, 15, 16) ]
}

kelly <- function(lst){
	min_ <- abs(min(lst))
	rate_  <- lst/min_
	pec_ <- seq(0.01, 0.99, 0.01)
	multy_ <- c()
	for(i in 1:length(pec_)){
		t <- 1
		for( r_ in rate_){
			t <- t * (1 + pec_[i] * r_)
		}
		multy_[i] <- t
	}
	result <- data.frame(pec = pec_, multy = multy_)
	plot(result$pec, result$multy)
	abline(h=1)
	result
}
