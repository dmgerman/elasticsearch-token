begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|delete
operator|.
name|DeleteResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|ClusterState
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
name|ESIntegTestCase
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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

begin_class
DECL|class|DateMathIndexExpressionsIntegrationIT
specifier|public
class|class
name|DateMathIndexExpressionsIntegrationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testIndexNameDateMathExpressions
specifier|public
name|void
name|testIndexNameDateMathExpressions
parameter_list|()
block|{
name|DateTime
name|now
init|=
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|String
name|index1
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
argument_list|)
decl_stmt|;
name|String
name|index2
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|index3
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|createIndex
argument_list|(
name|index1
argument_list|,
name|index2
argument_list|,
name|index3
argument_list|)
expr_stmt|;
name|String
name|dateMathExp1
init|=
literal|"<.marvel-{now/d}>"
decl_stmt|;
name|String
name|dateMathExp2
init|=
literal|"<.marvel-{now/d-1d}>"
decl_stmt|;
name|String
name|dateMathExp3
init|=
literal|"<.marvel-{now/d-2d}>"
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp1
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp2
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp3
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|dateMathExp1
argument_list|,
name|dateMathExp2
argument_list|,
name|dateMathExp3
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertSearchHits
argument_list|(
name|searchResponse
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|GetResponse
name|getResponse
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|dateMathExp1
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|dateMathExp2
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|dateMathExp3
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|IndicesStatsResponse
name|indicesStatsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|dateMathExp1
argument_list|,
name|dateMathExp2
argument_list|,
name|dateMathExp3
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index3
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteResponse
name|deleteResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|dateMathExp1
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|isFound
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|deleteResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|dateMathExp2
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|isFound
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|deleteResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|dateMathExp3
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|isFound
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreateIndexWithDateMathExpression
specifier|public
name|void
name|testAutoCreateIndexWithDateMathExpression
parameter_list|()
throws|throws
name|Exception
block|{
name|DateTime
name|now
init|=
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|String
name|index1
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
argument_list|)
decl_stmt|;
name|String
name|index2
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|index3
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|dateMathExp1
init|=
literal|"<.marvel-{now/d}>"
decl_stmt|;
name|String
name|dateMathExp2
init|=
literal|"<.marvel-{now/d-1d}>"
decl_stmt|;
name|String
name|dateMathExp3
init|=
literal|"<.marvel-{now/d-2d}>"
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp1
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp2
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|dateMathExp3
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|dateMathExp1
argument_list|,
name|dateMathExp2
argument_list|,
name|dateMathExp3
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertSearchHits
argument_list|(
name|searchResponse
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|IndicesStatsResponse
name|indicesStatsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|dateMathExp1
argument_list|,
name|dateMathExp2
argument_list|,
name|dateMathExp3
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStatsResponse
operator|.
name|getIndex
argument_list|(
name|index3
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateIndexWithDateMathExpression
specifier|public
name|void
name|testCreateIndexWithDateMathExpression
parameter_list|()
throws|throws
name|Exception
block|{
name|DateTime
name|now
init|=
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|String
name|index1
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
argument_list|)
decl_stmt|;
name|String
name|index2
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|index3
init|=
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|minusDays
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|dateMathExp1
init|=
literal|"<.marvel-{now/d}>"
decl_stmt|;
name|String
name|dateMathExp2
init|=
literal|"<.marvel-{now/d-1d}>"
decl_stmt|;
name|String
name|dateMathExp3
init|=
literal|"<.marvel-{now/d-2d}>"
decl_stmt|;
name|createIndex
argument_list|(
name|dateMathExp1
argument_list|,
name|dateMathExp2
argument_list|,
name|dateMathExp3
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index3
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

