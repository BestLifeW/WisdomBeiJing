package com.lovec.wisdom.domain;

import java.util.ArrayList;

/**
 * Created by lovec on 2016/8/26.
 */
public class PhotosBean {

    public PhotosData data;

    public class PhotosData {
        public ArrayList<PhotoNews> news;
    }

    public class PhotoNews {
        public int id;
        public String listimage;
        public String title;
    }
}

