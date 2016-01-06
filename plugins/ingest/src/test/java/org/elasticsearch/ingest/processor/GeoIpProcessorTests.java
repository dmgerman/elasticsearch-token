begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
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
name|core
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|GeoIpProcessor
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/GeoLite2-City.mmdb"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
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
name|Field
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
literal|10
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
literal|"North Holland"
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
literal|"Amsterdam"
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
literal|"Europe/Amsterdam"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"latitude"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|52.374
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoData
operator|.
name|get
argument_list|(
literal|"longitude"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|4.8897
argument_list|)
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
name|Arrays
operator|.
name|asList
argument_list|(
literal|4.8897d
argument_list|,
literal|52.374d
argument_list|)
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
name|GeoIpProcessor
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/GeoLite2-Country.mmdb"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
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
name|Field
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
name|GeoIpProcessor
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/GeoLite2-City.mmdb"
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
operator|new
name|GeoIpProcessor
argument_list|(
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
name|Field
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
literal|"202.45.11.11"
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
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

