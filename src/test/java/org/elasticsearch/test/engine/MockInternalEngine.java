begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|engine
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
name|search
operator|.
name|AssertingIndexSearcher
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|SearcherManager
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
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|EngineConfig
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
name|engine
operator|.
name|EngineException
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
name|engine
operator|.
name|InternalEngine
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

begin_class
DECL|class|MockInternalEngine
specifier|final
class|class
name|MockInternalEngine
extends|extends
name|InternalEngine
block|{
DECL|field|support
specifier|private
name|MockEngineSupport
name|support
decl_stmt|;
DECL|method|MockInternalEngine
name|MockInternalEngine
parameter_list|(
name|EngineConfig
name|config
parameter_list|,
name|boolean
name|skipInitialTranslogRecovery
parameter_list|)
throws|throws
name|EngineException
block|{
name|super
argument_list|(
name|config
argument_list|,
name|skipInitialTranslogRecovery
argument_list|)
expr_stmt|;
block|}
DECL|method|support
specifier|private
specifier|synchronized
name|MockEngineSupport
name|support
parameter_list|()
block|{
comment|// lazy initialized since we need it already on super() ctor execution :(
if|if
condition|(
name|support
operator|==
literal|null
condition|)
block|{
name|support
operator|=
operator|new
name|MockEngineSupport
argument_list|(
name|config
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|support
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|support
argument_list|()
operator|.
name|flushOrClose
argument_list|(
name|this
argument_list|,
name|MockEngineSupport
operator|.
name|CloseAction
operator|.
name|CLOSE
argument_list|)
condition|)
block|{
case|case
name|FLUSH_AND_CLOSE
case|:
name|super
operator|.
name|flushAndClose
argument_list|()
expr_stmt|;
break|break;
case|case
name|CLOSE
case|:
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Ongoing recoveries after engine close: "
operator|+
name|onGoingRecoveries
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|flushAndClose
specifier|public
name|void
name|flushAndClose
parameter_list|()
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|support
argument_list|()
operator|.
name|flushOrClose
argument_list|(
name|this
argument_list|,
name|MockEngineSupport
operator|.
name|CloseAction
operator|.
name|FLUSH_AND_CLOSE
argument_list|)
condition|)
block|{
case|case
name|FLUSH_AND_CLOSE
case|:
name|super
operator|.
name|flushAndClose
argument_list|()
expr_stmt|;
break|break;
case|case
name|CLOSE
case|:
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
break|break;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Ongoing recoveries after engine close: "
operator|+
name|onGoingRecoveries
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newSearcher
specifier|protected
name|Searcher
name|newSearcher
parameter_list|(
name|String
name|source
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|SearcherManager
name|manager
parameter_list|)
throws|throws
name|EngineException
block|{
specifier|final
name|Searcher
name|engineSearcher
init|=
name|super
operator|.
name|newSearcher
argument_list|(
name|source
argument_list|,
name|searcher
argument_list|,
name|manager
argument_list|)
decl_stmt|;
return|return
name|support
argument_list|()
operator|.
name|wrapSearcher
argument_list|(
name|source
argument_list|,
name|engineSearcher
argument_list|,
name|searcher
argument_list|,
name|manager
argument_list|)
return|;
block|}
block|}
end_class

end_unit

