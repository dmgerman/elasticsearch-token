begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|AbstractModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|inject
operator|.
name|Guice
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|Injector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|matcher
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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
name|Documented
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
name|*
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|InjectorsTests
specifier|public
class|class
name|InjectorsTests
block|{
DECL|method|testMatchers
annotation|@
name|Test
specifier|public
name|void
name|testMatchers
parameter_list|()
throws|throws
name|Exception
block|{
name|Injector
name|injector
init|=
name|Guice
operator|.
name|createInjector
argument_list|(
operator|new
name|MyModule
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|Injectors
operator|.
name|getInstancesOf
argument_list|(
name|injector
argument_list|,
name|A
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Injectors
operator|.
name|getInstancesOf
argument_list|(
name|injector
argument_list|,
name|B
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Injectors
operator|.
name|getInstancesOf
argument_list|(
name|injector
argument_list|,
name|C
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Injectors
operator|.
name|getInstancesOf
argument_list|(
name|injector
argument_list|,
name|Matchers
operator|.
name|subclassesOf
argument_list|(
name|C
operator|.
name|class
argument_list|)
operator|.
name|and
argument_list|(
name|Matchers
operator|.
name|annotatedWith
argument_list|(
name|Blue
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|MyModule
specifier|public
specifier|static
class|class
name|MyModule
extends|extends
name|AbstractModule
block|{
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|C
operator|.
name|class
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|B
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|A
specifier|public
specifier|static
class|class
name|A
block|{
DECL|field|name
specifier|public
name|String
name|name
init|=
literal|"A"
decl_stmt|;
block|}
DECL|class|B
specifier|public
specifier|static
class|class
name|B
extends|extends
name|A
block|{
DECL|method|B
specifier|public
name|B
parameter_list|()
block|{
name|name
operator|=
literal|"B"
expr_stmt|;
block|}
block|}
annotation|@
name|Blue
DECL|class|C
specifier|public
specifier|static
class|class
name|C
extends|extends
name|A
block|{
DECL|method|C
specifier|public
name|C
parameter_list|()
block|{
name|name
operator|=
literal|"C"
expr_stmt|;
block|}
block|}
annotation|@
name|Target
argument_list|(
block|{
name|METHOD
block|,
name|CONSTRUCTOR
block|,
name|FIELD
block|,
name|TYPE
block|}
argument_list|)
annotation|@
name|Retention
argument_list|(
name|RUNTIME
argument_list|)
annotation|@
name|Documented
annotation|@
name|BindingAnnotation
DECL|interface|Blue
specifier|public
annotation_defn|@interface
name|Blue
block|{     }
block|}
end_class

end_unit

