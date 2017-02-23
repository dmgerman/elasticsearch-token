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
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|db
operator|.
name|NoCache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|maxmind
operator|.
name|db
operator|.
name|NodeCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|Randomness
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
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|io
operator|.
name|IOException
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|Set
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
name|sameInstance
import|;
end_import

begin_class
DECL|class|GeoIpProcessorFactoryTests
specifier|public
class|class
name|GeoIpProcessorFactoryTests
extends|extends
name|ESTestCase
block|{
DECL|field|databaseReaders
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReaderLazyLoader
argument_list|>
name|databaseReaders
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|loadDatabaseReaders
specifier|public
specifier|static
name|void
name|loadDatabaseReaders
parameter_list|()
throws|throws
name|IOException
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
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
argument_list|)
argument_list|,
name|geoIpConfigDir
operator|.
name|resolve
argument_list|(
literal|"GeoLite2-City.mmdb.gz"
argument_list|)
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
literal|"/GeoLite2-Country.mmdb.gz"
argument_list|)
argument_list|)
argument_list|,
name|geoIpConfigDir
operator|.
name|resolve
argument_list|(
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
argument_list|)
expr_stmt|;
name|NodeCache
name|cache
init|=
name|randomFrom
argument_list|(
name|NoCache
operator|.
name|getInstance
argument_list|()
argument_list|,
operator|new
name|GeoIpCache
argument_list|(
name|randomNonNegativeLong
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|databaseReaders
operator|=
name|IngestGeoIpPlugin
operator|.
name|loadDatabaseReaders
argument_list|(
name|geoIpConfigDir
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|closeDatabaseReaders
specifier|public
specifier|static
name|void
name|closeDatabaseReaders
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|DatabaseReaderLazyLoader
name|reader
range|:
name|databaseReaders
operator|.
name|values
argument_list|()
control|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|databaseReaders
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testBuildDefaults
specifier|public
name|void
name|testBuildDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|String
name|processorTag
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|processorTag
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geoip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getDbReader
argument_list|()
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"GeoLite2-City"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProperties
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|GeoIpProcessor
operator|.
name|Factory
operator|.
name|DEFAULT_CITY_PROPERTIES
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetIgnoreMissing
specifier|public
name|void
name|testSetIgnoreMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"ignore_missing"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|processorTag
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|processorTag
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geoip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getDbReader
argument_list|()
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"GeoLite2-City"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProperties
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|GeoIpProcessor
operator|.
name|Factory
operator|.
name|DEFAULT_CITY_PROPERTIES
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCountryBuildDefaults
specifier|public
name|void
name|testCountryBuildDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
expr_stmt|;
name|String
name|processorTag
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|processorTag
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geoip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getDbReader
argument_list|()
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"GeoLite2-Country"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProperties
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|GeoIpProcessor
operator|.
name|Factory
operator|.
name|DEFAULT_COUNTRY_PROPERTIES
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildTargetField
specifier|public
name|void
name|testBuildTargetField
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"target_field"
argument_list|,
literal|"_field"
argument_list|)
expr_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildDbFile
specifier|public
name|void
name|testBuildDbFile
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
expr_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getTargetField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geoip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getDbReader
argument_list|()
operator|.
name|getMetadata
argument_list|()
operator|.
name|getDatabaseType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"GeoLite2-Country"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProperties
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|GeoIpProcessor
operator|.
name|Factory
operator|.
name|DEFAULT_COUNTRY_PROPERTIES
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildWithCountryDbAndCityFields
specifier|public
name|void
name|testBuildWithCountryDbAndCityFields
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
expr_stmt|;
name|EnumSet
argument_list|<
name|GeoIpProcessor
operator|.
name|Property
argument_list|>
name|cityOnlyProperties
init|=
name|EnumSet
operator|.
name|complementOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|ALL_COUNTRY_PROPERTIES
argument_list|)
decl_stmt|;
name|String
name|cityProperty
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|Randomness
operator|.
name|get
argument_list|()
argument_list|,
name|cityOnlyProperties
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"properties"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|cityProperty
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[properties] illegal property value ["
operator|+
name|cityProperty
operator|+
literal|"]. valid values are [IP, COUNTRY_ISO_CODE, COUNTRY_NAME, CONTINENT_NAME]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBuildNonExistingDbFile
specifier|public
name|void
name|testBuildNonExistingDbFile
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"does-not-exist.mmdb.gz"
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[database_file] database file [does-not-exist.mmdb.gz] doesn't exist"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBuildFields
specifier|public
name|void
name|testBuildFields
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|GeoIpProcessor
operator|.
name|Property
argument_list|>
name|properties
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numFields
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numFields
condition|;
name|i
operator|++
control|)
block|{
name|GeoIpProcessor
operator|.
name|Property
name|property
init|=
name|GeoIpProcessor
operator|.
name|Property
operator|.
name|values
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|properties
operator|.
name|add
argument_list|(
name|property
argument_list|)
expr_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
name|property
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"properties"
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
name|GeoIpProcessor
name|processor
init|=
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProperties
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|properties
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|processor
operator|.
name|isIgnoreMissing
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildIllegalFieldOption
specifier|public
name|void
name|testBuildIllegalFieldOption
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"properties"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"invalid"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[properties] illegal property value [invalid]. valid values are [IP, COUNTRY_ISO_CODE, "
operator|+
literal|"COUNTRY_NAME, CONTINENT_NAME, REGION_NAME, CITY_NAME, TIMEZONE, LOCATION]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"properties"
argument_list|,
literal|"invalid"
argument_list|)
expr_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[properties] property isn't a list, but of type [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLazyLoading
specifier|public
name|void
name|testLazyLoading
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
literal|"/GeoLite2-City.mmdb.gz"
argument_list|)
argument_list|)
argument_list|,
name|geoIpConfigDir
operator|.
name|resolve
argument_list|(
literal|"GeoLite2-City.mmdb.gz"
argument_list|)
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
literal|"/GeoLite2-Country.mmdb.gz"
argument_list|)
argument_list|)
argument_list|,
name|geoIpConfigDir
operator|.
name|resolve
argument_list|(
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Loading another database reader instances, because otherwise we can't test lazy loading as the the
comment|// database readers used at class level are reused between tests. (we want to keep that otherwise running this
comment|// test will take roughly 4 times more time)
name|Map
argument_list|<
name|String
argument_list|,
name|DatabaseReaderLazyLoader
argument_list|>
name|databaseReaders
init|=
name|IngestGeoIpPlugin
operator|.
name|loadDatabaseReaders
argument_list|(
name|geoIpConfigDir
argument_list|,
name|NoCache
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|GeoIpProcessor
operator|.
name|Factory
name|factory
init|=
operator|new
name|GeoIpProcessor
operator|.
name|Factory
argument_list|(
name|databaseReaders
argument_list|)
decl_stmt|;
for|for
control|(
name|DatabaseReaderLazyLoader
name|lazyLoader
range|:
name|databaseReaders
operator|.
name|values
argument_list|()
control|)
block|{
name|assertNull
argument_list|(
name|lazyLoader
operator|.
name|databaseReader
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"GeoLite2-City.mmdb.gz"
argument_list|)
expr_stmt|;
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|"_tag"
argument_list|,
name|config
argument_list|)
expr_stmt|;
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
literal|"_field"
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
literal|"database_file"
argument_list|,
literal|"GeoLite2-Country.mmdb.gz"
argument_list|)
expr_stmt|;
name|factory
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|"_tag"
argument_list|,
name|config
argument_list|)
expr_stmt|;
for|for
control|(
name|DatabaseReaderLazyLoader
name|lazyLoader
range|:
name|databaseReaders
operator|.
name|values
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|lazyLoader
operator|.
name|databaseReader
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

