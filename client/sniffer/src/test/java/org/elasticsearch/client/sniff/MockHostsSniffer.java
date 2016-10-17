begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.sniff
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|sniff
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
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
name|List
import|;
end_import

begin_comment
comment|/**  * Mock implementation of {@link HostsSniffer}. Useful to prevent any connection attempt while testing builders etc.  */
end_comment

begin_class
DECL|class|MockHostsSniffer
class|class
name|MockHostsSniffer
implements|implements
name|HostsSniffer
block|{
annotation|@
name|Override
DECL|method|sniffHosts
specifier|public
name|List
argument_list|<
name|HttpHost
argument_list|>
name|sniffHosts
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit
