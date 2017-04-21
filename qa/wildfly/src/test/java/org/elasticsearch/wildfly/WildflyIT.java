begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.wildfly
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|wildfly
package|;
end_package

begin_comment
DECL|package|org.elasticsearch.wildfly
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|methods
operator|.
name|CloseableHttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|methods
operator|.
name|HttpGet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|methods
operator|.
name|HttpPut
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|ContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|StringEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|CloseableHttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|HttpClientBuilder
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
name|util
operator|.
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
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
name|ClusterModule
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
name|NamedXContentRegistry
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|json
operator|.
name|JsonXContent
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|containsInAnyOrder
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|WildflyIT
specifier|public
class|class
name|WildflyIT
extends|extends
name|LuceneTestCase
block|{
DECL|method|testTransportClient
specifier|public
name|void
name|testTransportClient
parameter_list|()
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
try|try
init|(
name|CloseableHttpClient
name|client
init|=
name|HttpClientBuilder
operator|.
name|create
argument_list|()
operator|.
name|build
argument_list|()
init|)
block|{
specifier|final
name|String
name|str
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"http://localhost:38080/wildfly-%s%s/transport/employees/1"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|isSnapshot
argument_list|()
condition|?
literal|"-SNAPSHOT"
else|:
literal|""
argument_list|)
decl_stmt|;
specifier|final
name|HttpPut
name|put
init|=
operator|new
name|HttpPut
argument_list|(
operator|new
name|URI
argument_list|(
name|str
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
name|body
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"first_name"
argument_list|,
literal|"John"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"last_name"
argument_list|,
literal|"Smith"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"age"
argument_list|,
literal|25
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"about"
argument_list|,
literal|"I love to go rock climbing"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"interests"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|value
argument_list|(
literal|"sports"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"music"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|body
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
block|}
name|put
operator|.
name|setEntity
argument_list|(
operator|new
name|StringEntity
argument_list|(
name|body
argument_list|,
name|ContentType
operator|.
name|APPLICATION_JSON
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|CloseableHttpResponse
name|response
init|=
name|client
operator|.
name|execute
argument_list|(
name|put
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|201
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|HttpGet
name|get
init|=
operator|new
name|HttpGet
argument_list|(
operator|new
name|URI
argument_list|(
name|str
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|CloseableHttpResponse
name|response
init|=
name|client
operator|.
name|execute
argument_list|(
name|get
argument_list|)
init|;
name|XContentParser
name|parser
operator|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
operator|new
name|NamedXContentRegistry
argument_list|(
name|ClusterModule
operator|.
name|getNamedXWriteables
argument_list|()
argument_list|)
argument_list|,
name|response
operator|.
name|getEntity
argument_list|()
operator|.
name|getContent
argument_list|()
argument_list|)
init|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"first_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"John"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"last_name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"Smith"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"age"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|25
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"about"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"I love to go rock climbing"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Object
name|interests
init|=
name|map
operator|.
name|get
argument_list|(
literal|"interests"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|interests
argument_list|,
name|instanceOf
argument_list|(
name|List
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|interestsAsList
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|interests
decl_stmt|;
name|assertThat
argument_list|(
name|interestsAsList
argument_list|,
name|containsInAnyOrder
argument_list|(
literal|"sports"
argument_list|,
literal|"music"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

