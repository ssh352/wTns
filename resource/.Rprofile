options(repos="http://mirrors.tuna.tsinghua.edu.cn/CRAN/")

.First <- function(){
	mm <- 3
}

readQu <- function(file){
	raw <- read.table(file, header=TRUE, sep = ",", fileEncoding="GBK")
	names(raw) <- 1:length(names(raw))
	sel <- raw[, c(4, 13, 14, 15, 16) ]
}
