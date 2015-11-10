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
name|index
operator|.
name|FilterDirectoryReader
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
name|inject
operator|.
name|BindingAnnotation
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
name|inject
operator|.
name|Inject
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
name|EngineFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
operator|.
name|FIELD
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
operator|.
name|PARAMETER
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
operator|.
name|RUNTIME
import|;
end_import

begin_class
DECL|class|MockEngineFactory
specifier|public
specifier|final
class|class
name|MockEngineFactory
implements|implements
name|EngineFactory
block|{
DECL|field|wrapper
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|FilterDirectoryReader
argument_list|>
name|wrapper
decl_stmt|;
DECL|method|MockEngineFactory
specifier|public
name|MockEngineFactory
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|FilterDirectoryReader
argument_list|>
name|wrapper
parameter_list|)
block|{
name|this
operator|.
name|wrapper
operator|=
name|wrapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newReadWriteEngine
specifier|public
name|Engine
name|newReadWriteEngine
parameter_list|(
name|EngineConfig
name|config
parameter_list|,
name|boolean
name|skipTranslogRecovery
parameter_list|)
block|{
return|return
operator|new
name|MockInternalEngine
argument_list|(
name|config
argument_list|,
name|skipTranslogRecovery
argument_list|,
name|wrapper
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newReadOnlyEngine
specifier|public
name|Engine
name|newReadOnlyEngine
parameter_list|(
name|EngineConfig
name|config
parameter_list|)
block|{
return|return
operator|new
name|MockShadowEngine
argument_list|(
name|config
argument_list|,
name|wrapper
argument_list|)
return|;
block|}
block|}
end_class

end_unit

