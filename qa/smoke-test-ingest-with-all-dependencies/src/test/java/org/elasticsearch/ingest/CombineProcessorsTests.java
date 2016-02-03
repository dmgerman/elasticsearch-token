begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|collect
operator|.
name|HppcMaps
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentHelper
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
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
name|CompoundProcessor
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
name|core
operator|.
name|Pipeline
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
name|Processor
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
name|geoip
operator|.
name|GeoIpProcessor
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
name|geoip
operator|.
name|IngestGeoIpPlugin
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
name|grok
operator|.
name|GrokProcessor
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
name|grok
operator|.
name|IngestGrokPlugin
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
name|AppendProcessor
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
name|ConvertProcessor
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
name|DateProcessor
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
name|LowercaseProcessor
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
name|RemoveProcessor
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
name|RenameProcessor
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
name|SplitProcessor
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
name|TrimProcessor
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
name|UppercaseProcessor
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
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
name|nullValue
import|;
end_import

begin_class
DECL|class|CombineProcessorsTests
specifier|public
class|class
name|CombineProcessorsTests
extends|extends
name|ESTestCase
block|{
DECL|field|LOG
specifier|private
specifier|static
specifier|final
name|String
name|LOG
init|=
literal|"70.193.17.92 - - [08/Sep/2014:02:54:42 +0000] \"GET /presentations/logstash-scale11x/images/ahhh___rage_face_by_samusmmx-d5g5zap.png HTTP/1.1\" 200 175208 \"http://mobile.rivals.com/board_posts.asp?SID=880&mid=198829575&fid=2208&tid=198829575&Team=&TeamId=&SiteId=\" \"Mozilla/5.0 (Linux; Android 4.2.2; VS980 4G Build/JDQ39B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.135 Mobile Safari/537.36\""
decl_stmt|;
DECL|method|testLogging
specifier|public
name|void
name|testLogging
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|configDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|geoIpConfigDir
init|=
name|configDir
operator|.
name|resolve
argument_list|(
literal|"ingest-geoip"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|geoIpConfigDir
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|StreamsUtils
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/GeoLite2-City.mmdb"
argument_list|)
argument_list|)
argument_list|,
name|geoIpConfigDir
operator|.
name|resolve
argument_list|(
literal|"GeoLite2-City.mmdb"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReader
argument_list|>
name|databaseReaders
init|=
name|IngestGeoIpPlugin
operator|.
name|loadDatabaseReaders
argument_list|(
name|geoIpConfigDir
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"log"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"pattern"
argument_list|,
literal|"%{COMBINEDAPACHELOG}"
argument_list|)
expr_stmt|;
name|Processor
name|processor1
init|=
operator|new
name|GrokProcessor
operator|.
name|Factory
argument_list|(
name|IngestGrokPlugin
operator|.
name|loadBuiltinPatterns
argument_list|()
argument_list|)
operator|.
name|doCreate
argument_list|(
literal|null
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"response"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
expr_stmt|;
name|Processor
name|processor2
init|=
operator|new
name|ConvertProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"bytes"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
expr_stmt|;
name|Processor
name|processor3
init|=
operator|new
name|ConvertProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_field"
argument_list|,
literal|"timestamp"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
literal|"timestamp"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"match_formats"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"dd/MMM/YYYY:HH:mm:ss Z"
argument_list|)
argument_list|)
expr_stmt|;
name|Processor
name|processor4
init|=
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"source_field"
argument_list|,
literal|"clientip"
argument_list|)
expr_stmt|;
name|Processor
name|processor5
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
argument_list|)
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
operator|new
name|Pipeline
argument_list|(
literal|"_id"
argument_list|,
literal|"_description"
argument_list|,
operator|new
name|CompoundProcessor
argument_list|(
name|processor1
argument_list|,
name|processor2
argument_list|,
name|processor3
argument_list|,
name|processor4
argument_list|,
name|processor5
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"log"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|IngestDocument
name|document
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|pipeline
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|17
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"request"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"/presentations/logstash-scale11x/images/ahhh___rage_face_by_samusmmx-d5g5zap.png"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"agent"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"\"Mozilla/5.0 (Linux; Android 4.2.2; VS980 4G Build/JDQ39B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.135 Mobile Safari/537.36\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"auth"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"-"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"ident"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"-"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"verb"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"GET"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"referrer"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"\"http://mobile.rivals.com/board_posts.asp?SID=880&mid=198829575&fid=2208&tid=198829575&Team=&TeamId=&SiteId=\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"response"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"bytes"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|175208
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"clientip"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"70.193.17.92"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"httpversion"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1.1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"rawrequest"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"2014-09-08T02:54:42.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|geoInfo
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"geoip"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoInfo
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoInfo
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
name|geoInfo
operator|.
name|get
argument_list|(
literal|"city_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Charlotte"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoInfo
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
name|geoInfo
operator|.
name|get
argument_list|(
literal|"region_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"North Carolina"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoInfo
operator|.
name|get
argument_list|(
literal|"location"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|field|PERSON
specifier|private
specifier|static
specifier|final
name|String
name|PERSON
init|=
literal|"{\n"
operator|+
literal|"    \"age\": 33,\n"
operator|+
literal|"    \"eyeColor\": \"brown\",\n"
operator|+
literal|"    \"name\": \"Miranda Goodwin\",\n"
operator|+
literal|"    \"gender\": \"male\",\n"
operator|+
literal|"    \"company\": \"ATGEN\",\n"
operator|+
literal|"    \"email\": \"mirandagoodwin@atgen.com\",\n"
operator|+
literal|"    \"phone\": \"+1 (914) 489-3656\",\n"
operator|+
literal|"    \"address\": \"713 Bartlett Place, Accoville, Puerto Rico, 9221\",\n"
operator|+
literal|"    \"registered\": \"2014-11-23T08:34:21 -01:00\",\n"
operator|+
literal|"    \"tags\": [\n"
operator|+
literal|"      \"ex\",\n"
operator|+
literal|"      \"do\",\n"
operator|+
literal|"      \"occaecat\",\n"
operator|+
literal|"      \"reprehenderit\",\n"
operator|+
literal|"      \"anim\",\n"
operator|+
literal|"      \"laboris\",\n"
operator|+
literal|"      \"cillum\"\n"
operator|+
literal|"    ],\n"
operator|+
literal|"    \"friends\": [\n"
operator|+
literal|"      {\n"
operator|+
literal|"        \"id\": 0,\n"
operator|+
literal|"        \"name\": \"Wendi Odonnell\"\n"
operator|+
literal|"      },\n"
operator|+
literal|"      {\n"
operator|+
literal|"        \"id\": 1,\n"
operator|+
literal|"        \"name\": \"Mayra Boyd\"\n"
operator|+
literal|"      },\n"
operator|+
literal|"      {\n"
operator|+
literal|"        \"id\": 2,\n"
operator|+
literal|"        \"name\": \"Lee Gonzalez\"\n"
operator|+
literal|"      }\n"
operator|+
literal|"    ]\n"
operator|+
literal|"  }"
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testMutate
specifier|public
name|void
name|testMutate
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// TODO: when we add foreach processor we should delete all friends.id fields
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"friends.0.id"
argument_list|)
expr_stmt|;
name|RemoveProcessor
name|processor1
init|=
operator|new
name|RemoveProcessor
operator|.
name|Factory
argument_list|(
name|TestTemplateService
operator|.
name|instance
argument_list|()
argument_list|)
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"tags"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|"new_value"
argument_list|)
expr_stmt|;
name|AppendProcessor
name|processor2
init|=
operator|new
name|AppendProcessor
operator|.
name|Factory
argument_list|(
name|TestTemplateService
operator|.
name|instance
argument_list|()
argument_list|)
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"address"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"separator"
argument_list|,
literal|","
argument_list|)
expr_stmt|;
name|SplitProcessor
name|processor3
init|=
operator|new
name|SplitProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
comment|// TODO: when we add foreach processor, then change the test to trim all address values
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"address.1"
argument_list|)
expr_stmt|;
name|TrimProcessor
name|processor4
init|=
operator|new
name|TrimProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"company"
argument_list|)
expr_stmt|;
name|LowercaseProcessor
name|processor5
init|=
operator|new
name|LowercaseProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"gender"
argument_list|)
expr_stmt|;
name|UppercaseProcessor
name|processor6
init|=
operator|new
name|UppercaseProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|config
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"eyeColor"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"to"
argument_list|,
literal|"eye_color"
argument_list|)
expr_stmt|;
name|RenameProcessor
name|processor7
init|=
operator|new
name|RenameProcessor
operator|.
name|Factory
argument_list|()
operator|.
name|create
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
operator|new
name|Pipeline
argument_list|(
literal|"_id"
argument_list|,
literal|"_description"
argument_list|,
operator|new
name|CompoundProcessor
argument_list|(
name|processor1
argument_list|,
name|processor2
argument_list|,
name|processor3
argument_list|,
name|processor4
argument_list|,
name|processor5
argument_list|,
name|processor6
argument_list|,
name|processor7
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|PERSON
argument_list|)
argument_list|)
operator|.
name|map
argument_list|()
decl_stmt|;
name|IngestDocument
name|document
init|=
operator|new
name|IngestDocument
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|pipeline
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
operator|)
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"friends"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
operator|)
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"friends"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
operator|)
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"friends"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|get
argument_list|(
literal|"id"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"tags.7"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"new_value"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|addressDetails
init|=
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"address"
argument_list|,
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|addressDetails
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
name|addressDetails
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"713 Bartlett Place"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|addressDetails
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Accoville"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|addressDetails
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|" Puerto Rico"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|addressDetails
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|" 9221"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"company"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"atgen"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"gender"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"MALE"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|get
argument_list|(
literal|"eye_color"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"brown"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

