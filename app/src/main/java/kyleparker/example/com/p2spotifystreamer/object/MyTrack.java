package kyleparker.example.com.p2spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Custom {@link Track} object used to implement Parcelable.
 *
 * Created by kyleparker on 6/18/2015.
 */
public class MyTrack extends Track implements Parcelable {
    private String albumName;
    private String artistId;
    private String artistName;
    private String imageUrl;

    public MyArtist artist = new MyArtist();

    public String getAlbumName() {
        return albumName;
    }
    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistId() {
        return artistId;
    }
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MyTrack() { }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        try {
            parcel.writeString(id);
            parcel.writeString(name);
            parcel.writeString(imageUrl);
            parcel.writeInt(track_number);
            parcel.writeLong(duration_ms);
            parcel.writeString(preview_url);
            parcel.writeString(type);
            parcel.writeString(albumName);
            parcel.writeString(artistId);
            parcel.writeString(artistName);

            parcel.writeParcelable(artist, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private MyTrack(Parcel source) {
        id = source.readString();
        name = source.readString();
        imageUrl = source.readString();
        track_number = source.readInt();
        duration_ms = source.readLong();
        preview_url = source.readString();
        type = source.readString();
        albumName = source.readString();
        artistId = source.readString();
        artistName = source.readString();

        ClassLoader classLoader = getClass().getClassLoader();
        artist = source.readParcelable(classLoader);
    }

    public static final Creator<MyTrack> CREATOR = new Creator<MyTrack>() {
        @Override
        public MyTrack createFromParcel(Parcel in) {
            return new MyTrack(in);
        }

        @Override
        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };
}
