begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|settings
operator|.
name|Settings
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
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltAnalyzers
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PreBuiltAnalyzerIntegrationTests
specifier|public
class|class
name|PreBuiltAnalyzerIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testThatPreBuiltAnalyzersAreNotClosedOnIndexClose
specifier|public
name|void
name|testThatPreBuiltAnalyzersAreNotClosedOnIndexClose
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|PreBuiltAnalyzers
argument_list|,
name|List
argument_list|<
name|Version
argument_list|>
argument_list|>
name|loadedAnalyzers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|indexNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|String
name|indexName
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|indexNames
operator|.
name|add
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|int
name|randomInt
init|=
name|randomInt
argument_list|(
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|PreBuiltAnalyzers
name|preBuiltAnalyzer
init|=
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
index|[
name|randomInt
index|]
decl_stmt|;
name|String
name|name
init|=
name|preBuiltAnalyzer
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
decl_stmt|;
name|Version
name|randomVersion
init|=
name|randomVersion
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|loadedAnalyzers
operator|.
name|containsKey
argument_list|(
name|preBuiltAnalyzer
argument_list|)
condition|)
block|{
name|loadedAnalyzers
operator|.
name|put
argument_list|(
name|preBuiltAnalyzer
argument_list|,
name|Lists
operator|.
expr|<
name|Version
operator|>
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|loadedAnalyzers
operator|.
name|get
argument_list|(
name|preBuiltAnalyzer
argument_list|)
operator|.
name|add
argument_list|(
name|randomVersion
argument_list|)
expr_stmt|;
specifier|final
name|XContentBuilder
name|mapping
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
name|name
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Settings
name|versionSettings
init|=
name|randomSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|randomVersion
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|indexName
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mapping
argument_list|)
operator|.
name|setSettings
argument_list|(
name|versionSettings
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// index some amount of data
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|String
name|randomIndex
init|=
name|indexNames
operator|.
name|get
argument_list|(
name|randomInt
argument_list|(
name|indexNames
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|randomId
init|=
name|randomInt
argument_list|()
operator|+
literal|""
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|data
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|data
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|randomIndex
argument_list|,
literal|"type"
argument_list|,
name|randomId
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
comment|// close some of the indices
name|int
name|amountOfIndicesToClose
init|=
name|randomInt
argument_list|(
literal|10
operator|-
literal|1
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
name|amountOfIndicesToClose
condition|;
name|i
operator|++
control|)
block|{
name|String
name|indexName
init|=
name|indexNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClose
argument_list|(
name|indexName
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// check that all above configured analyzers have been loaded
name|assertThatAnalyzersHaveBeenLoaded
argument_list|(
name|loadedAnalyzers
argument_list|)
expr_stmt|;
comment|// check that all of the prebuiltanalyzers are still open
for|for
control|(
name|PreBuiltAnalyzers
name|preBuiltAnalyzer
range|:
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
control|)
block|{
name|assertLuceneAnalyzerIsNotClosed
argument_list|(
name|preBuiltAnalyzer
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertThatAnalyzersHaveBeenLoaded
specifier|private
name|void
name|assertThatAnalyzersHaveBeenLoaded
parameter_list|(
name|Map
argument_list|<
name|PreBuiltAnalyzers
argument_list|,
name|List
argument_list|<
name|Version
argument_list|>
argument_list|>
name|expectedLoadedAnalyzers
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|PreBuiltAnalyzers
argument_list|,
name|List
argument_list|<
name|Version
argument_list|>
argument_list|>
name|entry
range|:
name|expectedLoadedAnalyzers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|Version
argument_list|,
name|Analyzer
argument_list|>
name|cachedAnalyzers
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getCachedAnalyzers
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|cachedAnalyzers
operator|.
name|keySet
argument_list|()
argument_list|,
name|hasItems
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|Version
index|[]
block|{}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|/*for (Version expectedVersion : entry.getValue()) {                 assertThat(cachedAnalyzers, contains(ex))             }             */
block|}
block|}
comment|// the close() method of a lucene analyzer sets the storedValue field to null
comment|// we simply check this via reflection - ugly but works
DECL|method|assertLuceneAnalyzerIsNotClosed
specifier|private
name|void
name|assertLuceneAnalyzerIsNotClosed
parameter_list|(
name|PreBuiltAnalyzers
name|preBuiltAnalyzer
parameter_list|)
throws|throws
name|IllegalAccessException
throws|,
name|NoSuchFieldException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Version
argument_list|,
name|Analyzer
argument_list|>
name|luceneAnalyzerEntry
range|:
name|preBuiltAnalyzer
operator|.
name|getCachedAnalyzers
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Field
name|field
init|=
name|getFieldFromClass
argument_list|(
literal|"storedValue"
argument_list|,
name|luceneAnalyzerEntry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|currentAccessible
init|=
name|field
operator|.
name|isAccessible
argument_list|()
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Object
name|storedValue
init|=
name|field
operator|.
name|get
argument_list|(
name|preBuiltAnalyzer
operator|.
name|getAnalyzer
argument_list|(
name|luceneAnalyzerEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
name|currentAccessible
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Analyzer %s in version %s seems to be closed"
argument_list|,
name|preBuiltAnalyzer
operator|.
name|name
argument_list|()
argument_list|,
name|luceneAnalyzerEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|storedValue
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Searches for a field until it finds, loops through all superclasses      */
DECL|method|getFieldFromClass
specifier|private
name|Field
name|getFieldFromClass
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|obj
parameter_list|)
block|{
name|Field
name|field
init|=
literal|null
decl_stmt|;
name|boolean
name|storedValueFieldFound
init|=
literal|false
decl_stmt|;
name|Class
name|clazz
init|=
name|obj
operator|.
name|getClass
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|storedValueFieldFound
condition|)
block|{
try|try
block|{
name|field
operator|=
name|clazz
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|storedValueFieldFound
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
name|clazz
operator|=
name|clazz
operator|.
name|getSuperclass
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|Object
operator|.
name|class
operator|.
name|equals
argument_list|(
name|clazz
argument_list|)
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not find storedValue field in class"
operator|+
name|clazz
argument_list|)
throw|;
block|}
return|return
name|field
return|;
block|}
block|}
end_class

end_unit

