package com.cinestream.live;

public class Channel {
    private String num;
    private String name;
    private String stream_type;
    private String stream_id;
    private String stream_icon;
    private String epg_channel_id;
    private String added;
    private String category_name;
    private String category_id;
    private String series_no;
    private String live;
    private String container_extension;
    private String custom_sid;
    private String tv_archive;
    private String direct_source;
    private String tv_archive_duration;

    public Channel() {}

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStream_type() {
        return stream_type;
    }

    public void setStream_type(String stream_type) {
        this.stream_type = stream_type;
    }

    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getEpg_channel_id() {
        return epg_channel_id;
    }

    public void setEpg_channel_id(String epg_channel_id) {
        this.epg_channel_id = epg_channel_id;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getSeries_no() {
        return series_no;
    }

    public void setSeries_no(String series_no) {
        this.series_no = series_no;
    }

    public String getLive() {
        return live;
    }

    public void setLive(String live) {
        this.live = live;
    }

    public String getContainer_extension() {
        return container_extension;
    }

    public void setContainer_extension(String container_extension) {
        this.container_extension = container_extension;
    }

    public String getCustom_sid() {
        return custom_sid;
    }

    public void setCustom_sid(String custom_sid) {
        this.custom_sid = custom_sid;
    }

    public String getTv_archive() {
        return tv_archive;
    }

    public void setTv_archive(String tv_archive) {
        this.tv_archive = tv_archive;
    }

    public String getDirect_source() {
        return direct_source;
    }

    public void setDirect_source(String direct_source) {
        this.direct_source = direct_source;
    }

    public String getTv_archive_duration() {
        return tv_archive_duration;
    }

    public void setTv_archive_duration(String tv_archive_duration) {
        this.tv_archive_duration = tv_archive_duration;
    }

    public String getStreamUrl(String server, String username, String password) {
        if (stream_type != null && stream_type.equals("live")) {
            return server + "/" + username + "/" + password + "/" + stream_id;
        }
        return null;
    }
}