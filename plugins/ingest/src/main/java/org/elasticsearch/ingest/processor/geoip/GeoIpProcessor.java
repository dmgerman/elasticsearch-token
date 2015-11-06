begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.geoip
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|geoip
package|;
end_package

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|DatabaseReader
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|exception
operator|.
name|GeoIp2Exception
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|model
operator|.
name|CityResponse
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|model
operator|.
name|CountryResponse
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|geoip2
operator|.
name|record
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
operator|.
name|NetworkAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Data
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|Processor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardOpenOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|ConfigurationUtils
operator|.
name|readStringProperty
import|;
end_import

begin_class
DECL|class|GeoIpProcessor
specifier|public
specifier|final
class|class
name|GeoIpProcessor
implements|implements
name|Processor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"geoip"
decl_stmt|;
DECL|field|ipField
specifier|private
specifier|final
name|String
name|ipField
decl_stmt|;
DECL|field|targetField
specifier|private
specifier|final
name|String
name|targetField
decl_stmt|;
DECL|field|dbReader
specifier|private
specifier|final
name|DatabaseReader
name|dbReader
decl_stmt|;
DECL|method|GeoIpProcessor
name|GeoIpProcessor
parameter_list|(
name|String
name|ipField
parameter_list|,
name|DatabaseReader
name|dbReader
parameter_list|,
name|String
name|targetField
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|ipField
operator|=
name|ipField
expr_stmt|;
name|this
operator|.
name|targetField
operator|=
name|targetField
expr_stmt|;
name|this
operator|.
name|dbReader
operator|=
name|dbReader
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
name|String
name|ip
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|ipField
argument_list|)
decl_stmt|;
specifier|final
name|InetAddress
name|ipAddress
decl_stmt|;
try|try
block|{
name|ipAddress
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|ip
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
decl_stmt|;
switch|switch
condition|(
name|dbReader
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
condition|)
block|{
case|case
literal|"GeoLite2-City"
case|:
name|geoData
operator|=
name|retrieveCityGeoData
argument_list|(
name|ipAddress
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"GeoLite2-Country"
case|:
name|geoData
operator|=
name|retrieveCountryGeoData
argument_list|(
name|ipAddress
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported database type ["
operator|+
name|dbReader
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|data
operator|.
name|addField
argument_list|(
name|targetField
argument_list|,
name|geoData
argument_list|)
expr_stmt|;
block|}
DECL|method|getIpField
name|String
name|getIpField
parameter_list|()
block|{
return|return
name|ipField
return|;
block|}
DECL|method|getTargetField
name|String
name|getTargetField
parameter_list|()
block|{
return|return
name|targetField
return|;
block|}
DECL|method|getDbReader
name|DatabaseReader
name|getDbReader
parameter_list|()
block|{
return|return
name|dbReader
return|;
block|}
DECL|method|retrieveCityGeoData
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|retrieveCityGeoData
parameter_list|(
name|InetAddress
name|ipAddress
parameter_list|)
block|{
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|CityResponse
name|response
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|CityResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CityResponse
name|run
parameter_list|()
block|{
try|try
block|{
return|return
name|dbReader
operator|.
name|city
argument_list|(
name|ipAddress
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|GeoIp2Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|Country
name|country
init|=
name|response
operator|.
name|getCountry
argument_list|()
decl_stmt|;
name|City
name|city
init|=
name|response
operator|.
name|getCity
argument_list|()
decl_stmt|;
name|Location
name|location
init|=
name|response
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|Continent
name|continent
init|=
name|response
operator|.
name|getContinent
argument_list|()
decl_stmt|;
name|Subdivision
name|subdivision
init|=
name|response
operator|.
name|getMostSpecificSubdivision
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"ip"
argument_list|,
name|NetworkAddress
operator|.
name|formatAddress
argument_list|(
name|ipAddress
argument_list|)
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"country_iso_code"
argument_list|,
name|country
operator|.
name|getIsoCode
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"country_name"
argument_list|,
name|country
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"continent_name"
argument_list|,
name|continent
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"region_name"
argument_list|,
name|subdivision
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"city_name"
argument_list|,
name|city
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"timezone"
argument_list|,
name|location
operator|.
name|getTimeZone
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"latitude"
argument_list|,
name|location
operator|.
name|getLatitude
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"longitude"
argument_list|,
name|location
operator|.
name|getLongitude
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|location
operator|.
name|getLatitude
argument_list|()
operator|!=
literal|null
operator|&&
name|location
operator|.
name|getLongitude
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|geoData
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
operator|new
name|double
index|[]
block|{
name|location
operator|.
name|getLongitude
argument_list|()
block|,
name|location
operator|.
name|getLatitude
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|geoData
return|;
block|}
DECL|method|retrieveCountryGeoData
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|retrieveCountryGeoData
parameter_list|(
name|InetAddress
name|ipAddress
parameter_list|)
block|{
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|CountryResponse
name|response
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|CountryResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|CountryResponse
name|run
parameter_list|()
block|{
try|try
block|{
return|return
name|dbReader
operator|.
name|country
argument_list|(
name|ipAddress
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|GeoIp2Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|Country
name|country
init|=
name|response
operator|.
name|getCountry
argument_list|()
decl_stmt|;
name|Continent
name|continent
init|=
name|response
operator|.
name|getContinent
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"ip"
argument_list|,
name|NetworkAddress
operator|.
name|formatAddress
argument_list|(
name|ipAddress
argument_list|)
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"country_iso_code"
argument_list|,
name|country
operator|.
name|getIsoCode
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"country_name"
argument_list|,
name|country
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|geoData
operator|.
name|put
argument_list|(
literal|"continent_name"
argument_list|,
name|continent
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|geoData
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
argument_list|<
name|GeoIpProcessor
argument_list|>
block|{
DECL|field|geoIpConfigDirectory
specifier|private
name|Path
name|geoIpConfigDirectory
decl_stmt|;
DECL|field|databaseReaderService
specifier|private
specifier|final
name|DatabaseReaderService
name|databaseReaderService
init|=
operator|new
name|DatabaseReaderService
argument_list|()
decl_stmt|;
DECL|method|create
specifier|public
name|GeoIpProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|ipField
init|=
name|readStringProperty
argument_list|(
name|config
argument_list|,
literal|"ip_field"
argument_list|)
decl_stmt|;
name|String
name|targetField
init|=
name|readStringProperty
argument_list|(
name|config
argument_list|,
literal|"target_field"
argument_list|,
literal|"geoip"
argument_list|)
decl_stmt|;
name|String
name|databaseFile
init|=
name|readStringProperty
argument_list|(
name|config
argument_list|,
literal|"database_file"
argument_list|,
literal|"GeoLite2-City.mmdb"
argument_list|)
decl_stmt|;
name|Path
name|databasePath
init|=
name|geoIpConfigDirectory
operator|.
name|resolve
argument_list|(
name|databaseFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|databasePath
argument_list|)
operator|&&
name|Files
operator|.
name|isRegularFile
argument_list|(
name|databasePath
argument_list|)
condition|)
block|{
try|try
init|(
name|InputStream
name|database
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|databasePath
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|)
init|)
block|{
name|DatabaseReader
name|databaseReader
init|=
name|databaseReaderService
operator|.
name|getOrCreateDatabaseReader
argument_list|(
name|databaseFile
argument_list|,
name|database
argument_list|)
decl_stmt|;
return|return
operator|new
name|GeoIpProcessor
argument_list|(
name|ipField
argument_list|,
name|databaseReader
argument_list|,
name|targetField
argument_list|)
return|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"database file ["
operator|+
name|databaseFile
operator|+
literal|"] doesn't exist in ["
operator|+
name|geoIpConfigDirectory
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|setConfigDirectory
specifier|public
name|void
name|setConfigDirectory
parameter_list|(
name|Path
name|configDirectory
parameter_list|)
block|{
name|geoIpConfigDirectory
operator|=
name|configDirectory
operator|.
name|resolve
argument_list|(
literal|"ingest"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"geoip"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|databaseReaderService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

