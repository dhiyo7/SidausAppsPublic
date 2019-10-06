package dev7.id.sidausappspublic.Server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtil {
//    public static final String ENDPOINT = "http://dpmptsp.brebeskab.go.id:8080/";
    public static final String BASE_URL = "http://dpmptsp.brebeskab.go.id:8080/";

    private static Retrofit retrofit;

    private static Retrofit getApiClient(){
        if (retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static UserInterface getUserInterface() { return getApiClient().create(UserInterface.class); }
    public static KecamatanInterface getKecamatanInterface() { return getApiClient().create(KecamatanInterface.class); }
    public static DesaInterface getDesaInterface() { return getApiClient().create(DesaInterface.class); }
    public static UsahaInterface getUsahaInterface() { return getApiClient().create(UsahaInterface.class); }

}
