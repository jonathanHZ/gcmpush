/**
 * Copyright 2015  Jeroen van Vianen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.vanvianen.android.gcm;

public class GCMNotificationManager {

    public Bitmap getImageFromUrl(String urlIcon) {

		InputStream in;
        try {

	    	URL url = new URL(urlIcon);
	    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    	connection.setDoInput(true);
	    	connection.connect();
	    	in = connection.getInputStream();
	    	Bitmap myBitmap = BitmapFactory.decodeStream(in);
	    	return myBitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;	
    }
}