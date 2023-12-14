#
# Copyright (c) 2023 Airbyte, Inc., all rights reserved.
#

import re
import os

from pipelines.airbyte_ci.connectors.context import ConnectorContext
from pipelines.airbyte_ci.connectors.reports import ConnectorReport
from pipelines.helpers import git
from pipelines.models.steps import Step, StepResult, StepStatus


class SetCDKVersion(Step):
    title = "Set CDK Version"

    def __init__(
        self,
        context: ConnectorContext,
        new_version: str,
    ):
        super().__init__(context)
        self.new_version = new_version

    async def _run(self) -> StepResult:
        context: ConnectorContext = self.context
        connector_dir = await context.get_connector_dir(include=["setup.py"])
        if not (await connector_dir.entries()):
            return StepResult(
                self,
                StepStatus.FAILURE,
                stdout="Connector does not have a setup.py file.",
            )
        setup_py = connector_dir.file("setup.py")
        setup_py_content = await setup_py.contents()
        try:
            updated_setup_py = self.update_cdk_version(setup_py_content)
            full_og_connector_dir = await context.get_connector_dir()
            updated_connector_dir = full_og_connector_dir.with_new_file("setup.py", updated_setup_py)
            diff = full_og_connector_dir.diff(updated_connector_dir)
            await diff.export(os.path.join(git.get_git_repo_path(), context.connector.code_directory))
            return StepResult(
                self,
                StepStatus.SUCCESS,
                stdout=f"Updated CDK version to {self.new_version}",
                output_artifact=diff
            )
        except Exception as e:
            return StepResult(
                self,
                StepStatus.FAILURE,
                stderr=f"Could not set CDK version: {e}",
                exc_info=e,
            )

    def update_cdk_version(self, og_setup_py_content: str) -> str:
        airbyte_cdk_dependency = re.search(
            r"airbyte-cdk(?P<extra>\[[a-zA-Z0-9-]*\])?(?P<version>[<>=!~]+[0-9]*\.[0-9]*\.[0-9]*)?", og_setup_py_content
        )
        # If there is no airbyte-cdk dependency, add the version
        if airbyte_cdk_dependency is not None:
            new_version = f"airbyte-cdk{airbyte_cdk_dependency.group('extra') or ''}>={self.new_version}"
            return og_setup_py_content.replace(airbyte_cdk_dependency.group(), new_version)
        else:
            raise ValueError("Could not find airbyte-cdk dependency in setup.py")

async def run_connector_cdk_upgrade_pipeline(
    context: ConnectorContext,
    semaphore,
    target_version: str,
) -> ConnectorReport:
    """Run a pipeline to upgrade the CDK version for a single connector.

    Args:
        context (ConnectorContext): The initialized connector context.

    Returns:
        ConnectorReport: The reports holding the CDK version set results.
    """
    async with semaphore:
        steps_results = []
        async with context:
            set_cdk_version = SetCDKVersion(
                context,
                target_version,
            )
            set_cdk_version_result = await set_cdk_version.run()
            steps_results.append(set_cdk_version_result)
            context.report = ConnectorReport(context, steps_results, name="CONNECTOR VERSION CDK UPGRADE RESULTS")
    return context.report
