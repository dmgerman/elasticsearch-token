begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch (the "Author") under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Author licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.s3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|s3
package|;
end_package

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
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|S3SnapshotRestoreOverHttpTest
specifier|public
class|class
name|S3SnapshotRestoreOverHttpTest
extends|extends
name|S3SnapshotRestoreAbstractTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|public
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|settings
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"cloud.aws.s3.protocol"
argument_list|,
literal|"http"
argument_list|)
decl_stmt|;
return|return
name|settings
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

