begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|common
operator|.
name|Nullable
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
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|ScriptPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchHit
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|3
argument_list|)
DECL|class|ScriptFieldIT
specifier|public
class|class
name|ScriptFieldIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|CustomScriptPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|field|intArray
specifier|static
name|int
index|[]
name|intArray
init|=
block|{
name|Integer
operator|.
name|MAX_VALUE
block|,
name|Integer
operator|.
name|MIN_VALUE
block|,
literal|3
block|}
decl_stmt|;
DECL|field|longArray
specifier|static
name|long
index|[]
name|longArray
init|=
block|{
name|Long
operator|.
name|MAX_VALUE
block|,
name|Long
operator|.
name|MIN_VALUE
block|,
literal|9223372036854775807L
block|}
decl_stmt|;
DECL|field|floatArray
specifier|static
name|float
index|[]
name|floatArray
init|=
block|{
name|Float
operator|.
name|MAX_VALUE
block|,
name|Float
operator|.
name|MIN_VALUE
block|,
literal|3.3f
block|}
decl_stmt|;
DECL|field|doubleArray
specifier|static
name|double
index|[]
name|doubleArray
init|=
block|{
name|Double
operator|.
name|MAX_VALUE
block|,
name|Double
operator|.
name|MIN_VALUE
block|,
literal|3.3d
block|}
decl_stmt|;
DECL|method|testNativeScript
specifier|public
name|void
name|testNativeScript
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc1"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc2"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc3"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"4"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc4"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc5"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"6"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"doc6"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"int"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"int"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"native"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"float"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"float"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"native"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"double"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"double"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"native"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"long"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"long"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"native"
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
control|)
block|{
name|Object
name|result
init|=
name|hit
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"int"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|intArray
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|hit
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"long"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|longArray
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|hit
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"float"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|floatArray
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|=
name|hit
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"double"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|doubleArray
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|IntArrayScriptFactory
specifier|public
specifier|static
class|class
name|IntArrayScriptFactory
implements|implements
name|NativeScriptFactory
block|{
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|IntScript
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"int"
return|;
block|}
block|}
DECL|class|IntScript
specifier|static
class|class
name|IntScript
extends|extends
name|AbstractSearchScript
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|intArray
return|;
block|}
block|}
DECL|class|LongArrayScriptFactory
specifier|public
specifier|static
class|class
name|LongArrayScriptFactory
implements|implements
name|NativeScriptFactory
block|{
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|LongScript
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"long"
return|;
block|}
block|}
DECL|class|LongScript
specifier|static
class|class
name|LongScript
extends|extends
name|AbstractSearchScript
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|longArray
return|;
block|}
block|}
DECL|class|FloatArrayScriptFactory
specifier|public
specifier|static
class|class
name|FloatArrayScriptFactory
implements|implements
name|NativeScriptFactory
block|{
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|FloatScript
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"float"
return|;
block|}
block|}
DECL|class|FloatScript
specifier|static
class|class
name|FloatScript
extends|extends
name|AbstractSearchScript
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|floatArray
return|;
block|}
block|}
DECL|class|DoubleArrayScriptFactory
specifier|public
specifier|static
class|class
name|DoubleArrayScriptFactory
implements|implements
name|NativeScriptFactory
block|{
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|DoubleScript
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"double"
return|;
block|}
block|}
DECL|class|DoubleScript
specifier|static
class|class
name|DoubleScript
extends|extends
name|AbstractSearchScript
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|doubleArray
return|;
block|}
block|}
DECL|class|CustomScriptPlugin
specifier|public
specifier|static
class|class
name|CustomScriptPlugin
extends|extends
name|Plugin
implements|implements
name|ScriptPlugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"custom_script"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"script "
return|;
block|}
annotation|@
name|Override
DECL|method|getNativeScripts
specifier|public
name|List
argument_list|<
name|NativeScriptFactory
argument_list|>
name|getNativeScripts
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|IntArrayScriptFactory
argument_list|()
argument_list|,
operator|new
name|LongArrayScriptFactory
argument_list|()
argument_list|,
operator|new
name|FloatArrayScriptFactory
argument_list|()
argument_list|,
operator|new
name|DoubleArrayScriptFactory
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

