package com.aviary.android.picturethis;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;


public class Instructions extends Activity {
    private TextView helpData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
        helpData = (TextView)findViewById(R.id.textView);

        final String htmlText = "<br/><br/><br/><br/><div align='center'><ul><br/><br/>" +
                "<li><img border=\"0\" src=\"plus_web.png\" alt=\"nothing\"> : Open/Load Favorite Images from Gallery</li><br/><br/>" +
                "<li><img border=\"0\" src=\"single_tap.png\" alt=\"nothing\"> : Delete the image</li><br/><br/>" +
                "<li><img border=\"0\" src=\"long_press.png\" alt=\"nothing\"> : Edit the image</li><br/><br/>" +
                "<li><img border=\"0\" src=\"web_video.png\" alt=\"nothing\"> : Create Video of the images</li><br/><br/>" +
                "<li>Share : FB/Email</li><br/>" +
                "</ul><br/>" +
                "<br/>Be an Artist and Have Fun!" +
                "</div>";

        helpData.setText(Html.fromHtml(htmlText, new ImageGetter(), null));

    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }

    private class ImageGetter implements Html.ImageGetter {

        public Drawable getDrawable(String source) {
            int id;
            if (source.equals("plus_web.png")) {
                id = R.drawable.plus_web;
            }
            else if (source.equals("single_tap.png"))
            {
                id = R.drawable.single_tap;
            }
            else if (source.equals("long_press.png"))
            {
                id = R.drawable.long_press;
            }
            else if (source.equals("web_video.png"))
            {
                id = R.drawable.web_video;
            }
            else{
                return null;
            }

            Drawable d = getResources().getDrawable(id);
            d.setBounds(0,0,50,50);
            return d;
        }
    }

}



