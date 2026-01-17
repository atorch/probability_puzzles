set_ggplot_theme <- function(map=FALSE) {
    require(ggplot2)
    require(grid)  # For unit
    theme_set(theme_bw())
    ## For axis titles: margin instead of vjust
    ## See https://stackoverflow.com/questions/14487188/increase-distance-between-text-and-title-on-the-y-axis
    theme_update(axis.title.x=element_text(size=rel(1.4), margin=margin(10, 0, 0, 0)))
    theme_update(axis.title.y=element_text(angle=90, size=rel(1.4),
                                           margin=margin(0, 20, 0, 0)))
    theme_update(axis.text.x=element_text(size=rel(1.2)))
    theme_update(axis.text.y=element_text(size=rel(1.2)))
    theme_update(plot.margin=unit(c(1, 1, 2, 2), "lines"))
    theme_update(plot.title=element_text(size=rel(1.4), vjust=1.25))
    theme_update(strip.background=element_rect(colour="white"))
    theme_update(legend.key=element_blank())
    theme_update(panel.border=element_blank())
    theme_update(panel.grid.minor=element_blank())
    if (map) {
        theme_update(axis.text=element_blank())
        theme_update(axis.ticks=element_blank())
        theme_update(legend.background=element_blank())
        theme_update(legend.margin=unit(0, "lines"))
        theme_update(legend.position="bottom")
        theme_update(panel.border=element_blank())
        theme_update(panel.grid.major=element_blank())
        theme_update(panel.grid.minor=element_blank())
    }
    return(invisible())
}
legend_guide <- guide_legend(direction="horizontal",
                             title.position="top",
                             label.position="bottom",
                             label.hjust=0.5, label.vjust=2,
                             title.hjust=0.5,
                             label.theme=element_text(angle=0),
                             keywidth=3.5, keyheight=1)
