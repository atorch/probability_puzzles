## From the Android docs
## https://developer.android.com/guide/topics/resources/drawable-resource
## "A drawable resource is a general concept for a graphic that can be drawn to the screen"
## This script creates the graphics that are shown underneath certain puzzle descriptions
## For example, app/src/main/res/drawable-hdpi/backgammon_small.png
## is shown in a puzzle about the probability of certain jumps in the game of backgammon

library(ggplot2)
library(grid)
library(optparse)

source("utils.R")

opt_list <- list(make_option("--filename_suffix", default="", type="character"),
                 make_option("--voter_x_label", default="voter 4", type="character"),
                 make_option("--voter_y_label", default="voter 3", type="character"))
opt <- parse_args(OptionParser(option_list=opt_list))

message("command line options: ", paste(sprintf("%s=%s", names(opt), opt), collapse=", "))

set_ggplot_theme()
theme_update(panel.grid.major=element_blank())
theme_update(panel.grid.minor=element_blank())
theme_update(axis.text=element_text(size=rel(1.6)))
theme_update(axis.text.x=element_text(size=rel(1.6)))
theme_update(axis.text.y=element_text(size=rel(1.6)))
df <- data.frame(x=c(0, 0, 1, 1),
                 y=c(0, -1, 0, -1),
                 label=c("?", "?", "?", "0.6*2/3"))
colors <- c("#a6611a", "#018571")
jitter <- 0.05
p <- (ggplot(df, aes(x=x, y=y, label=label)) +
      geom_rect(xmin=-0.5+jitter, xmax=1.5+jitter, ymin=-1.5+jitter, ymax=-0.5+jitter,
                fill="white", color=colors[1], size=3.0, alpha=0) +  # Horizontal
      annotate("text", x=1.8, y=-1+jitter, color=colors[1], label="0.6", size=11) +
      geom_rect(xmin=0.5, xmax=1.5, ymin=-1.5, ymax=0.5, fill="white", color=colors[2], size=3.0, alpha=0) +  # Vertical
      annotate("text", x=1, y=0.65, color=colors[2], label="0.6", size=11) +
      geom_text(size=8) +
      scale_y_continuous(opt$voter_y_label, breaks=c(-1, 0), labels=c("C", "T"), lim=c(-2, 1)) +
      scale_x_continuous(opt$voter_x_label, breaks=c(0, 1), labels=c("T", "C"), lim=c(-1, 2)))
p
ggsave(sprintf("CT_vote_table%s.png", opt$filename_suffix), p, width=8, height=8)

set_ggplot_theme()
df <- data.frame(number_bets=seq(0, 11), fortune=c(seq(1, 6), seq(5, 0)))
df$fortune_alternate <- cumsum(c(1, 1, 1, -1, -1, 1, -1, 1, -1, 1, -1, -1))
p <- (ggplot(df, aes(x=number_bets, y=fortune)) +
      geom_segment(aes(x=0, y=0, xend=max(df$number_bets), yend=0), size=1.5, color="grey", linetype=2) +
      geom_point(aes(x=0, y=1), color="black", size=4) +
      geom_line(size=3, color="black") +
      geom_line(aes(x=number_bets, y=fortune_alternate), size=3, color="black") +
      geom_point(aes(x=max(df$number_bets), y=0), color="#E69F00", size=6) +
      scale_x_continuous("number of bets completed", breaks=c(0, 5, 10)) +
      ylab("gambler's fortune"))
p
ggsave("gambler_broke_11_bets.png", p, width=8, height=6)

color_left <- "#386cb0"
color_right <- "#7fc97f"
breakpoint <- 0.4
p <- (ggplot(data.frame(x=c(0, 1), y=c(0, 0)), aes(x=x, y=y)) +
      geom_segment(x=0, y=0, xend=breakpoint, yend=0, size=3, color=color_left) +
      geom_segment(x=breakpoint, y=0, xend=1.0, yend=0, size=3, color=color_right) +
      annotate("text", x=breakpoint/2, y=0.01, label="left piece", color=color_left, size=10) +
      annotate("text", x=(breakpoint + 1)/2, y=0.01, label="right piece", color=color_right, size=10) +
      scale_x_continuous("", breaks=c(0, breakpoint, 1), labels=c("0", "uniform random breakpoint", "1"),
                         lim=c(0, 1)) +
      scale_y_continuous("", breaks=NULL, lim=c(-0.02, 0.05)))
p
ggsave("random_breakpoint.png", p, width=8, height=4)

votes_C <- 70
votes_T <- 30
df <- data.frame(counted=seq(0, votes_C + votes_T))
df$lower_bound <- cumsum(c(0, rep(-1, votes_T), rep(1, votes_C)))
df$upper_bound <- cumsum(c(0, rep(1, votes_C), rep(-1, votes_T)))
df$path1 <- c(0, cumsum(c(rep(1, 8),
                          sample(c(rep(1, votes_C-8), rep(-1, votes_T)), size=votes_C + votes_T - 8, replace=FALSE))))
df$path2 <- c(0, cumsum(c(rep(1, 5),
                          rep(-1, 5),
                          sample(c(rep(1, votes_C-5), rep(-1, votes_T-5)), size=votes_C + votes_T - 10, replace=FALSE))))
df$path2_color <- ifelse(df$counted <= 10, "before", "after")
scale_color <- c("before"="#E69F00", "after"="black")

p <- (ggplot(df, aes(x=counted, y=upper_bound)) +
      geom_segment(aes(x=0, y=0, xend=2*votes_T, yend=0), size=1.5, color="grey", linetype=2) +
      geom_point(aes(x=0, y=0), color="black", size=4) +
      geom_point(aes(x=max(df$counted), y=votes_C - votes_T), color="black", size=4) +
      geom_line(size=3.0, color="black") +
      geom_line(aes(x=counted, y=lower_bound), size=3.0, color="black") +
      geom_line(aes(x=counted, y=path1), size=2.0, color="black") +
      geom_line(aes(x=counted, y=path2), size=2.0, color="black") +
      geom_point(aes(x=10, y=0), color="#E69F00", size=8, shape="O") +  # No longer strictly in the lead
      scale_color_manual("", values=scale_color, guide=FALSE) +
      xlab("number of votes counted") + ylab("net votes for C"))
p
ggsave("vote_counting.png", p, width=8, height=6)

votes_C <- 7
votes_T <- 3
df <- data.frame(counted=seq(0, votes_C + votes_T))
df$lower_bound <- cumsum(c(0, rep(-1, votes_T), rep(1, votes_C)))
df$upper_bound <- cumsum(c(0, rep(1, votes_C), rep(-1, votes_T)))
df$path1 <- c(0, cumsum(c(rep(1, 2),
                          sample(c(rep(1, votes_C-2), rep(-1, votes_T)), size=votes_C + votes_T - 2, replace=FALSE))))
df$path2 <- c(0, cumsum(c(rep(1, 2),
                          rep(-1, 2),
                          sample(c(rep(1, votes_C-2), rep(-1, votes_T-2)), size=votes_C + votes_T - 4, replace=FALSE))))
p <- (ggplot(df, aes(x=counted, y=upper_bound)) +
      geom_segment(aes(x=0, y=0, xend=2*votes_T, yend=0), size=1.5, color="grey", linetype=2) +
      geom_point(aes(x=0, y=0), color="black", size=4) +
      geom_point(aes(x=max(df$counted), y=votes_C - votes_T), color="black", size=4) +
      geom_line(size=3.0, color="black") +
      geom_line(aes(x=counted, y=lower_bound), size=3.0, color="black") +
      geom_line(aes(x=counted, y=path1), size=2.0, color="black") +
      geom_line(aes(x=counted, y=path2), size=2.0, color="black") +
      geom_point(aes(x=4, y=0), color="#E69F00", size=8, shape="O") +  # No longer strictly in the lead
      scale_color_manual("", values=scale_color, guide=FALSE) +
      scale_x_continuous("number of votes counted", breaks=seq(0, 10, 2)) +
      scale_y_continuous("net votes for C", breaks=seq(-2, 6, 2)))
p
ggsave("vote_counting_easy.png", p, width=8, height=6)

df <- data.frame(x=c(0, 100), y=c(20, 80))
p <- (ggplot(df, aes(x=x, y=y)) +
      geom_abline(intercept=0, slope=1, color="grey", linetype=2, size=2) +
      geom_line(size=3, color="#009E73") +
      scale_y_continuous("child's income percentile", limits=c(0, 100),
                         breaks=c(0, 20, 100)) +
      scale_x_continuous("parents' income percentile", breaks=c(0, 100)))
p
ggsave("income.png", p, width=8, height=6)

draw_curve1 <- curveGrob(0, 1, 1, 0,  # Top left to bottom right
                         default.units="npc",
                         curvature=-0.3, angle=90, ncp=20, shape=1,
                         square=FALSE, squareShape=1,
                         inflect=FALSE, arrow=arrow(), open=TRUE,
                         debug=FALSE, name=NULL, gp=gpar(lwd=3), vp=NULL)
draw_curve2 <- curveGrob(0, 1, 1, 0,  # Top left to bottom right, used for 0 to 6 jump in backgammon
                         default.units="npc",
                         curvature=-0.5, angle=90, ncp=20, shape=1,
                         square=FALSE, squareShape=1,
                         inflect=FALSE, arrow=arrow(), open=TRUE,
                         debug=FALSE, name=NULL, gp=gpar(lwd=3), vp=NULL)
draw_curve3 <- curveGrob(0, 0, 1, 1,  # Bottom left to top right
                         default.units="npc",
                         curvature=-0.4, angle=90, ncp=20, shape=1,
                         square=FALSE, squareShape=1,
                         inflect=FALSE, arrow=arrow(), open=TRUE,
                         debug=FALSE, name=NULL, gp=gpar(lwd=3), vp=NULL)
draw_curve4 <- curveGrob(1, 1, 0, 0,  # Top right to bottom left
                         default.units="npc",
                         curvature=0.5, angle=90, ncp=20, shape=1,
                         square=FALSE, squareShape=1,
                         inflect=FALSE, arrow=arrow(), open=TRUE,
                         debug=FALSE, name=NULL, gp=gpar(lwd=3), vp=NULL)
draw_curve5 <- curveGrob(1, 0, 0, 1,  # Bottom right to top left
                         default.units="npc",
                         curvature=-0.3, angle=90, ncp=20, shape=1,
                         square=FALSE, squareShape=1,
                         inflect=FALSE, arrow=arrow(), open=TRUE,
                         debug=FALSE, name=NULL, gp=gpar(lwd=3), vp=NULL)

set_ggplot_theme()
df <- data.frame(x=seq(0, 6), y=0)
p <- (ggplot(df, aes(x=x, y=y)) +
      scale_x_continuous("", breaks=seq(0, 6), limits=c(-0.75, 6.75)) +
      scale_y_continuous("", breaks=c(), limits=c(-0.01, 0.10)) +
      geom_hline(yintercept=0, size=1.25) +
      geom_rect(xmin=-0.4, xmax=0.4, ymin=0.005, ymax=0.02, color="black", fill="black") +
      geom_rect(xmin=-0.4, xmax=0.4, ymin=0.025, ymax=0.04, color="black", fill="black") +
      geom_rect(xmin=5.6, xmax=6.4, ymin=0.005, ymax=0.02, color="grey", fill="grey") +
      annotation_custom(grob=draw_curve1, 0, 3.95, 0.005, 0.045) +  # Little jump
      annotation_custom(grob=draw_curve2, 0, 6, 0.026, 0.045) +  # Big jump from 0 to 6
      annotation_custom(grob=draw_curve3, 4.05, 5.8, 0.005, 0.023) +  # Second little jump
      geom_blank())
p
ggsave("backgammon.png", p, width=10, height=6)

set_ggplot_theme(map=TRUE)
n <- 4
x <- seq(0, n)
df <- data.frame(p=dbinom(x, n, prob=0.35), x=x)
stopifnot(sum(df$p) == 1)
p <- (ggplot(df, aes(x=x, y=p)) +
      geom_bar(stat="identity", color="black", fill="white", size=5) +
      xlab("") + ylab(""))
p
ggsave("probility_mass.png", p, width=10, height=8)  # Unused

set_ggplot_theme()
theme_update(panel.grid.major=element_line(size=0.6, color="grey90"))
theme_update(panel.grid.minor=element_blank())
range <- 1
df <- expand.grid(x=seq(-range, range), y=seq(-range, range))
df <- subset(df, (x != 0 & y != 0) | (y == 0 & x == 0))
my_arrow <- arrow(length=unit(1, "cm"))
end <- 0.85
line_color <-"#999999"
p <- (ggplot(df, aes(x=x, y=y)) +
      xlim((range + 0.5)*c(-1, 1)) + ylim((range + 0.5)*c(-1, 1)) +
      xlab("") + ylab("") +
      geom_segment(aes(x=0, y=0, xend=end, yend=end), arrow=my_arrow, size=2,
                   color=line_color) +
      geom_segment(aes(x=-0, y=0, xend=-end, yend=end), arrow=my_arrow, size=2,
                   color=line_color) +
      geom_segment(aes(x=0, y=-0, xend=end, yend=-end), arrow=my_arrow, size=2,
                   color=line_color) +
      geom_segment(aes(x=-0, y=-0, xend=-end, yend=-end), arrow=my_arrow, size=2,
                   color=line_color) +
      geom_point(size=12, color="black"))
p
ggsave("particle_large.png", p, width=10, height=8)
ggsave("particle_small.png", p, width=5, height=4)

set_ggplot_theme(map=TRUE)
n_guests <- 9
theta <- head(seq(0, 2*pi, length.out=n_guests + 2), n_guests + 1)
df <- data.frame(theta=theta)
df$is_host <- FALSE
df$is_host[3] <- TRUE
df$x <- cos(theta)
df$y <- sin(theta)
p <- (ggplot(df, aes(x=x, y=y, color=is_host)) +
      scale_color_manual("", guide=F, values=c("black", "#E69F00")) +
      xlim(c(-1.1, 1.1)) + ylim(c(-1.1, 1.1)) +
      annotate("text", x=df$x[2] + 0.12, y=df$y[2] + 0.15, label="A", color="black", size=10) +
      annotate("text", x=df$x[1] - 0.2, y=df$y[1] + 0.05, label="B", color="black", size=10) +
      annotate("text", x=df$x[10] + 0.12, y=df$y[10] - 0.1, label="C", color="black", size=10) +
      annotate("text", x=df$x[3] - 0.25, y=df$y[3] + 0.12, label="host",
               color="#E69F00", size=10) +
      annotate("text", x=df$x[4] - 0.25, y=df$y[4] + 0.1, label="you",
               color="black", size=10) +
      annotation_custom(grob=draw_curve2,
                        df$x[3], df$x[2] - 0.01,
                        df$y[3], df$y[2] + 0.1) +  # From host to A
      annotation_custom(grob=draw_curve2,
                        df$x[2], df$x[1] + 0.05,
                        df$y[2], df$y[1] + 0.1) +  # From A to B
      annotation_custom(grob=draw_curve5,
                        df$x[1], df$x[2] + 0,
                        df$y[1], df$y[2] - 0.1) +  # From B back to A
      annotation_custom(grob=draw_curve4,
                        df$x[1], df$x[10] - 0,
                        df$y[1], df$y[10] + 0.1) +  # From B to C
      geom_point(shape=20, size=14) +  # Draw points after arrows
      xlab("") + ylab(""))
p
ggsave("table.png", p, width=6, height=6)

## Sum equals 12
set_ggplot_theme()
df <- expand.grid(first=seq(1, 6), second=seq(1, 6))
df$condition <- (df$first + df$second) == 12
p <- (ggplot(df, aes(x=first, y=second, color=condition, shape=condition)) +
      scale_color_manual("", guide=F, values=c("#0072B2", "#D55E00")) +
      scale_shape_manual("", guide=F, values=c(1, 19)) +
      xlab("first roll") + ylab("second roll") +
      scale_x_continuous(breaks=seq(1, 6)) +
      scale_y_continuous(breaks=seq(1, 6)) +
      geom_point(size=10))
p
ggsave("roll_12.png", p, width=6, height=5)  # Unused

## Sum equals 7
set_ggplot_theme()
df <- expand.grid(first=seq(1, 6), second=seq(1, 6))
df$condition <- (df$first + df$second) == 7
df$sum <- df$first + df$second
p <- (ggplot(df, aes(x=first, y=second, color=condition, shape=condition)) +
      scale_color_manual("", guide=F, values=c("#0072B2", "#D55E00")) +
      scale_shape_manual("", guide=F, values=c(1, 19)) +
      xlab("first roll") + ylab("second roll") +
      scale_x_continuous(breaks=seq(1, 6)) +
      scale_y_continuous(breaks=seq(1, 6)) +
      geom_point(size=10))
p
ggsave("roll_7.png", p, width=6, height=5)  # Unused

p <- (ggplot(df, aes(x=first, y=second, color=sum)) +
      xlab("first roll") + ylab("second roll") +
      scale_color_gradient2("", guide=FALSE, midpoint=7,
                            low="#f0f0f0", mid="#1c9099", high="#f0f0f0") +
      scale_x_continuous(breaks=seq(1, 6)) +
      scale_y_continuous(breaks=seq(1, 6)) +
      geom_point(size=10))
p
ggsave("roll_7.png", p, width=6, height=5)

## Two girls out of two children
set_ggplot_theme()
theme_update(axis.text=element_text(size=rel(1.6)))
theme_update(axis.text.x=element_text(size=rel(1.6)))
theme_update(axis.text.y=element_text(size=rel(1.6)))
n_children <- 2
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
df$condition <- df$girls == 2
p <- (ggplot(df, aes(x=girls, y=prob, color=condition)) +
      geom_bar(fill="white", size=4, stat="identity") +
      scale_color_manual("", guide=F, values=c("#999999", "#009E73")) +
      xlab("number of girls") + ylab("probability"))
p
ggsave(filename="two_girls.png", p, width=6, height=4)  # Unused

n_children <- 2
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=2, color="grey") +
      geom_point(size=10, shape=16) +
      ylab("probability") + ylim(0, 0.55) +
      scale_x_continuous("number of girls", breaks=df$girls))
p
ggsave(filename="two_girls_v2.png", p, width=6, height=4)

n_children <- 2
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=12, color="grey") +
      ylab("probability") + ylim(0, 0.55) +
      scale_x_continuous("number of girls", breaks=df$girls, limits=c(-0.1, n_children + 0.1)))
p
ggsave(filename="two_girls_v3.png", p, width=6, height=4)

## One girl out of three children
n_children <- 3
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
df$condition <- df$girls == 1
p <- (ggplot(df, aes(x=girls, y=prob, color=condition)) +
      geom_bar(fill="white", size=4, stat="identity") +
      scale_color_manual("", guide=F, values=c("#999999", "#009E73")) +
      xlab("number of girls") + ylab("probability"))
p
ggsave(filename="one_girl_out_of_three.png", p, width=6, height=4)  # Unused

set_ggplot_theme()
n_children <- 3
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=2, color="grey") +
      geom_point(size=10, shape=16) +
      ylab("probability") + ylim(0, 0.45) +
      scale_x_continuous("number of girls", breaks=df$girls))
p
ggsave(filename="one_girl_out_of_three_v2.png", p, width=6, height=4)

n_children <- 3
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=12, color="grey") +
      ylab("probability") + ylim(0, 0.45) +
      scale_x_continuous("number of girls", breaks=df$girls, limits=c(-0.1, n_children + 0.1)))
p
ggsave(filename="one_girl_out_of_three_v3.png", p, width=6, height=4)

## Eight children
n_children <- 8
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
df$condition <- df$girls %in% c(0, 8)
p <- (ggplot(df, aes(x=girls, y=prob, color=condition)) +
      geom_bar(fill="white", size=4, stat="identity") +
      scale_color_manual("", guide=F, values=c("#999999", "#009E73")) +
      scale_x_continuous("number of girls", breaks=df$girls) +
      ylab("probability"))
p
ggsave(filename="eight_children.png", p, width=8, height=4)  # Unused

n_children <- 8
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=2, color="grey") +
      geom_point(size=10, shape=16) +
      ylab("probability") + ylim(0, 0.30) +
      scale_x_continuous("number of girls", breaks=df$girls))
p
ggsave(filename="eight_children_v2.png", p, width=8, height=4)

n_children <- 8
n_girls <- seq(0, n_children)
df <- data.frame(girls=n_girls, prob=dbinom(x=n_girls, size=n_children, prob=0.49))
p <- (ggplot(df, aes(x=girls, y=prob)) +
      geom_segment(aes(x=girls, xend=girls, yend=prob), y=0, size=12, color="grey") +
      ylab("probability") + ylim(0, 0.30) +
      scale_x_continuous("number of girls", breaks=df$girls, limits=c(-0.1, n_children + 0.1)))
p
ggsave(filename="eight_children_v3.png", p, width=8, height=4)

## Probability of joining team, four shots or six
set_ggplot_theme()
four <- function(skill) {
    return(1 - pbinom(q=1, size=4, prob=skill))
}
six <- function(skill) {
    return(1 - pbinom(q=2, size=6, prob=skill))
}
color_4 <- "#0072B2"
color_6 <- "#D55E00"
p <- (ggplot(data.frame(x=c(0, 1)), aes(x=x)) +
      stat_function(fun=four, color=color_4, size=2) +
      stat_function(fun=six, color=color_6, size=2) +
      scale_y_continuous("probability of joining", breaks=c(0, 0.5, 1)) +
      annotate("text", x=0.3, y=0.68, label="four shots", color=color_4, size=8) +
      annotate("text", x=0.52, y=0.35, label="six shots", color=color_6, size=8) +
      scale_x_continuous("skill", breaks=c(0, 1)))
p
ggsave("proba_join.png", p, width=6, height=5)

## Random walk
get_df <- function(rounds=61) {
    wealth <- rep(NA, rounds)
    goes_broke <- FALSE
    wealth[1] <- 2
    last_round <- rounds-1
    for(t in seq(2, length(wealth))) {
        wealth[t] <- wealth[t-1] + sample(c(1, -1), size=1, prob=c(3/4, 1/4))
        if(wealth[t] <= 0) {
            goes_broke <- TRUE
            last_round <- t-1
            break
        }
    }
    return(data.frame(t=seq(0, rounds-1), wealth=wealth, last_round=last_round,
                      group=round(runif(1, 0, 10^6)), goes_broke=goes_broke))
}

df <- do.call(rbind, replicate(30, get_df(), simplify=F))
df$broke <- df$wealth == 0
set_ggplot_theme()
p <- (ggplot(df, aes(x=t, y=wealth, group=group, color=goes_broke)) +
      geom_line(size=2) +
      geom_line(data=subset(df, goes_broke), size=2) +
      geom_point(data=subset(df, goes_broke & t == last_round), size=5) +
      scale_color_manual("", guide=FALSE, values=c("black", "#D55E00")) +
      ## geom_hline(color="#D55E00", yintercept=0, linetype=2, size=1.5) +
      xlab("rounds played") + ylab("dollars"))
p
ggsave("gambling.png", p, width=6, height=5)

f <- function(x) {
    df <- data.frame(a=c(0, 2, 10, 30, 70), b=c(2, 10, 30, 70, 90))
    stopifnot(all(df$b[1:(nrow(df)-1)] == df$a[2:nrow(df)]))
    stopifnot(all(df$a < df$b))
    conditional <- rep(0, nrow(df))
    proba <- rep(0, nrow(df))
    for(i in seq_len(nrow(df))) {
        if(x > df$b[i]) next
        conditional[i] <- 0.5 * (max(x, df$a[i]) + df$b[i])
        if(x < df$a[i]) {
            proba[i] <- 0.2
        } else {
            proba[i] <- 0.2 * (df$b[i] - x) / (df$b[i] - df$a[i])
        }
    }
    stopifnot(length(conditional) == length(proba))
    stopifnot(length(conditional) == nrow(df))
    proba <- proba / sum(proba)
    return(sum(proba * conditional))
}
f_vec <- Vectorize(f)
curve(f_vec, from=0, to=90)
curve(f_vec(x) - x, from=0, to=90)
f_vec(30)  # 65

set_ggplot_theme()
p <- (ggplot(data.frame(x=c(0, 90)), aes(x=x)) +
      stat_function(fun=f_vec, color="#0072B2", size=2) +
      geom_abline(intercept=0, slope=1, color="grey", size=2, linetype=2) +
      scale_y_continuous("life expectancy", breaks=c(0, 31.4, 90), limits=c(0, 90)) +
      scale_x_continuous("age", breaks=seq(0, 90, 30)))
p
ggsave("life_expectancy.png", p, width=6, height=5)
