begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.shaded.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|shaded
operator|.
name|test
package|;
end_package

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
name|client
operator|.
name|Client
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
name|logging
operator|.
name|ESLoggerFactory
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
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
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
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShadedIT
specifier|public
class|class
name|ShadedIT
extends|extends
name|LuceneTestCase
block|{
DECL|method|testStartShadedNode
specifier|public
name|void
name|testStartShadedNode
parameter_list|()
block|{
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
literal|"ERROR"
argument_list|)
expr_stmt|;
name|Path
name|data
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|data
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.mode"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
literal|"http.enabled"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NodeBuilder
name|builder
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|data
argument_list|(
literal|true
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|loadConfigSettings
argument_list|(
literal|false
argument_list|)
operator|.
name|local
argument_list|(
literal|true
argument_list|)
decl_stmt|;
try|try
init|(
name|Node
name|node
init|=
name|builder
operator|.
name|node
argument_list|()
init|)
block|{
name|Client
name|client
init|=
name|node
operator|.
name|client
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{ \"field\" : \"value\" }"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testLoadShadedClasses
specifier|public
name|void
name|testLoadShadedClasses
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"org.elasticsearch.common.cache.LoadingCache"
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
literal|"org.elasticsearch.common.joda.time.DateTime"
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
literal|"org.elasticsearch.common.util.concurrent.jsr166e.LongAdder"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ClassNotFoundException
operator|.
name|class
argument_list|)
DECL|method|testGuavaIsNotOnTheCP
specifier|public
name|void
name|testGuavaIsNotOnTheCP
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"com.google.common.cache.LoadingCache"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ClassNotFoundException
operator|.
name|class
argument_list|)
DECL|method|testJodaIsNotOnTheCP
specifier|public
name|void
name|testJodaIsNotOnTheCP
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"org.joda.time.DateTime"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ClassNotFoundException
operator|.
name|class
argument_list|)
DECL|method|testjsr166eIsNotOnTheCP
specifier|public
name|void
name|testjsr166eIsNotOnTheCP
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"com.twitter.jsr166e.LongAdder"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

