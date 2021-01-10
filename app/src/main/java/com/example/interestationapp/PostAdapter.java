package com.example.interestationapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Request;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

public class PostAdapter extends ArrayAdapter<Post> {
    final static String baseUrl = "https://interestation.azurewebsites.net/userFiles/";
    Context context;
    int resource;
    List<Post> postList;
    RequestQueue requestQueue;

    PostAdapter(Context context, int resource, List<Post> postList, RequestQueue requestQueue)
    {
        super(context,resource, postList);
        this.context = context;
        this.resource = resource;
        this.postList = postList;
        this.requestQueue = requestQueue;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource,null,false);
        TextView owner = view.findViewById(R.id.owner_name_lbl);
        TextView date = view.findViewById(R.id.date_lbl);
        TextView text = view.findViewById(R.id.post_text_lbl);
        TextView likesCount = view.findViewById(R.id.likesCount);
        ImageView image = view.findViewById(R.id.post_imageview);
        ImageView ownerImage = view.findViewById(R.id.profilePic);

        ImageButton likeBtn = view.findViewById(R.id.likeBtn);

        Post post = postList.get(position);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testLikeGuid = UUID.randomUUID().toString(); //generate random GUID

                //send POST request (create new like)
                try{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("likeId", testLikeGuid);
                    jsonObject.put("userId", post.ownerId);
                    jsonObject.put("user", JSONObject.NULL);
                    jsonObject.put("postId", post.id);
                    jsonObject.put("post", JSONObject.NULL);

                    final String mRequestBody = jsonObject.toString();

                    String url = "https://interestation.azurewebsites.net/api/v1/Likes";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("LOG_VOLLEY", response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("LOG_VOLLEY", error.toString());
                        }
                    }
                    ){
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }
                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                                return null;
                            }
                        }
                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };


                    requestQueue.add(stringRequest);

                } catch (JSONException e){
                    e.printStackTrace();
                }
                Toast.makeText(getContext(), "Liked post by " + post.ownerNick, Toast.LENGTH_SHORT).show();
            }
        });

        owner.setText(post.ownerNick);
        date.setText(post.date.toString());
        text.setText(post.text);

        String likeNumDisplay = post.likes.size() + "";
        likesCount.setText(likeNumDisplay);

        if(post.image.equals("null")){
            image.setVisibility(View.GONE);
        }else {
            String url = baseUrl + post.ownerId + "/" + post.id + "/" + post.image;
            Picasso.get().load(url).into(image);
            url = baseUrl + post.ownerId + "/" + post.ownerImg;
            Picasso.get().load(url).into(ownerImage);
        }

        return view;
    }
}
