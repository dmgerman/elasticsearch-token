begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.geoip
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
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
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|RandomDocumentPicks
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
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|util
operator|.
name|EnumSet
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
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|GZIPInputStream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|GeoIpProcessorTests
specifier|public
class|class
name|GeoIpProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCity
specifier|public
name|void
name|testCity
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"8.8.8.8"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"source_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"8.8.8.8"
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"target_field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"ip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"8.8.8.8"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_iso_code"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"US"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"United States"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"continent_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"North America"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"region_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"California"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"city_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Mountain View"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"timezone"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"America/Los_Angeles"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|location
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|location
operator|.
name|put
argument_list|(
literal|"lat"
argument_list|,
literal|37.386d
argument_list|)
expr_stmt|;
name|location
operator|.
name|put
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|122.0838d
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"location"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|location
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCity_withIpV6
specifier|public
name|void
name|testCity_withIpV6
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|address
init|=
literal|"2602:306:33d3:8000::3257:9652"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"source_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"target_field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"ip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_iso_code"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"US"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"United States"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"continent_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"North America"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"region_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Florida"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"city_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Hollywood"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"timezone"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"America/New_York"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|location
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|location
operator|.
name|put
argument_list|(
literal|"lat"
argument_list|,
literal|26.0252d
argument_list|)
expr_stmt|;
name|location
operator|.
name|put
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|80.296d
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"location"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|location
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCityWithMissingLocation
specifier|public
name|void
name|testCityWithMissingLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"93.114.45.13"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"source_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"93.114.45.13"
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"target_field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"ip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"93.114.45.13"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCountry
specifier|public
name|void
name|testCountry
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-Country.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"82.170.213.79"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"source_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"82.170.213.79"
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"target_field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"ip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"82.170.213.79"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_iso_code"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"NL"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"country_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Netherlands"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"continent_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Europe"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCountryWithMissingLocation
specifier|public
name|void
name|testCountryWithMissingLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-Country.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"93.114.45.13"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"source_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"93.114.45.13"
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"target_field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"ip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"93.114.45.13"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAddressIsNotInTheDatabase
specifier|public
name|void
name|testAddressIsNotInTheDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"target_field"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Don't silently do DNS lookups or anything trappy on bogus data */
DECL|method|testInvalid
specifier|public
name|void
name|testInvalid
parameter_list|()
throws|throws
name|Exception
block|{
name|InputStream
name|database
init|=
name|getDatabaseFileInputStream
argument_list|(
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
literal|"source_field"
argument_list|,
operator|new
name|DatabaseReader
operator|.
name|Builder
argument_list|(
name|database
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|"target_field"
argument_list|,
name|EnumSet
operator|.
name|allOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"www.google.com"
argument_list|)
expr_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|,
name|document
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"did not get expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|expected
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"not an IP string literal"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getDatabaseFileInputStream
specifier|private
specifier|static
name|InputStream
name|getDatabaseFileInputStream
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|GZIPInputStream
argument_list|(
name|GeoIpProcessor
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

