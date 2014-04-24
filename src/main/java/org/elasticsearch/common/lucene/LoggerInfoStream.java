begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
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
name|InfoStream
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
name|ESLogger
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
name|Loggers
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
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_comment
comment|/** An InfoStream (for Lucene's IndexWriter) that redirects  *  messages to Logger.trace. */
end_comment

begin_class
DECL|class|LoggerInfoStream
specifier|public
specifier|final
class|class
name|LoggerInfoStream
extends|extends
name|InfoStream
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|LoggerInfoStream
specifier|public
name|LoggerInfoStream
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"lucene.iw"
argument_list|,
name|settings
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|message
specifier|public
name|void
name|message
parameter_list|(
name|String
name|component
parameter_list|,
name|String
name|message
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{}: {}"
argument_list|,
name|component
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
DECL|method|isEnabled
specifier|public
name|boolean
name|isEnabled
parameter_list|(
name|String
name|component
parameter_list|)
block|{
comment|// TP is a special "test point" component; we don't want
comment|// to log it:
return|return
name|logger
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|component
operator|.
name|equals
argument_list|(
literal|"TP"
argument_list|)
operator|==
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
end_class

end_unit

