
library(lattice)
data <<- numeric(100)

function(dataHolder) {
    svg()
    data <<- c(data[2:100], dataHolder$value)

    plot <- xyplot(randomData~time,
       data=data.frame(randomData = data, time = -99:0),
       main='Col-7 Plot',
       ylab="Data Value", type = c('l', 'g'), col.line='#654321')
    print(plot)
    svg.off()
}
