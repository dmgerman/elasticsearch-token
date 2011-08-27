begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river.couchdb
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|couchdb
package|;
end_package

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
name|delete
operator|.
name|DeleteIndexRequest
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
name|ImmutableSettings
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
name|IndexMissingException
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

begin_comment
comment|/**  * This is a simple test case for testing attachements removing.<br>  * You may have a couchdb instance running on localhost:5984 with a mytest database.<br>  * If you push documents with attachements in it, attachements should be ignored by the river.  * @author dadoonet (David Pilato)  */
end_comment

begin_class
DECL|class|CouchdbRiverAttachementTest
specifier|public
class|class
name|CouchdbRiverAttachementTest
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
name|String
name|port
init|=
literal|"5984"
decl_stmt|;
name|String
name|db
init|=
literal|"mytest"
decl_stmt|;
name|boolean
name|ignoreAttachements
init|=
literal|true
decl_stmt|;
name|Node
name|node
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|DeleteIndexRequest
argument_list|(
literal|"_river"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// Index does not exist... Fine
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|DeleteIndexRequest
argument_list|(
name|db
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// Index does not exist... Fine
block|}
name|XContentBuilder
name|xb
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"couchdb"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"couchdb"
argument_list|)
operator|.
name|field
argument_list|(
literal|"host"
argument_list|,
name|host
argument_list|)
operator|.
name|field
argument_list|(
literal|"port"
argument_list|,
name|port
argument_list|)
operator|.
name|field
argument_list|(
literal|"db"
argument_list|,
name|db
argument_list|)
operator|.
name|field
argument_list|(
literal|"ignoreAttachements"
argument_list|,
name|ignoreAttachements
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"_river"
argument_list|,
name|db
argument_list|,
literal|"_meta"
argument_list|)
operator|.
name|setSource
argument_list|(
name|xb
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100000
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

